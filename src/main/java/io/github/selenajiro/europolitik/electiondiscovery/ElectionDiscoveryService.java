package io.github.selenajiro.europolitik.electiondiscovery;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEvent;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventRepository;
import io.github.selenajiro.europolitik.rulingpartyimport.WikipediaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class ElectionDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(ElectionDiscoveryService.class);
    private static final String SOURCE = "wikipedia-election-discovery";
    private static final long DELAY_BETWEEN_CALLS_MS = 2000;
    private static final int MAX_RETRIES = 4;
    private static final long BASE_BACKOFF_MS = 5000;

    private static final Map<String, String> CATEGORY_NAME_ALIASES = Map.of(
            "Czechia", "Czech Republic"
    );

    private static final List<String> LEADERSHIP_PATTERNS = List.of(
            "leadership election", "leadership contest", "party primary"
    );

    private static final List<String> BY_ELECTION_PATTERNS = List.of(
            "by-election", "by election"
    );

    private static final List<String> LOCAL_PATTERNS = List.of(
            "local election", "municipal", "council election", "district election"
    );

    private static final List<String> REGIONAL_PATTERNS = List.of(
            "state election", "regional election", "gubernatorial", "senate election",
            "consular", "head election"
    );

    private static final Pattern REFERENDUM_PATTERN = Pattern.compile("referendum", Pattern.CASE_INSENSITIVE);

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(\\d{1,2}\\s+(?:January|February|March|April|May|June|July|August|September|October|November|December)\\s+\\d{4})\\b");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    private enum EventClassification {
        NATIONAL_ELECTION, LEADERSHIP_ELECTION, REFERENDUM,
        REGIONAL_ELECTION, LOCAL_ELECTION, BY_ELECTION, EXCLUDED
    }

    private final WikipediaClient client;
    private final CountryRepository countryRepository;
    private final PoliticalEventRepository politicalEventRepository;
    private final ImportRunRepository importRunRepository;

    public ElectionDiscoveryService(WikipediaClient client,
                                    CountryRepository countryRepository,
                                    PoliticalEventRepository politicalEventRepository,
                                    ImportRunRepository importRunRepository) {
        this.client = client;
        this.countryRepository = countryRepository;
        this.politicalEventRepository = politicalEventRepository;
        this.importRunRepository = importRunRepository;
    }

    @Transactional
    public ImportRun discoverElections() {
        ImportRun run = startRun();

        int read = 0;
        int inserted = 0;
        int skipped = 0;
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = List.of(currentYear - 1, currentYear);

        try {
            List<Country> countries = countryRepository.findAll();

            for (Country country : countries) {
                String categoryCountryName = CATEGORY_NAME_ALIASES.getOrDefault(country.getName(), country.getName());

                for (int year : years) {
                    List<String> titles;
                    try {
                        sleepBetweenCalls();
                        titles = withRetryOnRateLimit(() -> client.fetchElectionCategoryMembers(categoryCountryName, year));
                    } catch (Exception e) {
                        log.warn("Category lookup failed for {} ({}): {}", country.getName(), year, e.getMessage());
                        skipped++;
                        continue;
                    }

                    for (String title : titles) {
                        read++;
                        EventClassification classification = classify(title);
                        if (classification == EventClassification.EXCLUDED) {
                            skipped++;
                            continue;
                        }

                        if (politicalEventRepository.findBySourceArticleTitle(title).isPresent()) {
                            skipped++;
                            continue;
                        }

                        String eventType = switch (classification) {
                            case NATIONAL_ELECTION -> "ELECTION_HELD";
                            case LEADERSHIP_ELECTION -> "PARTY_LEADERSHIP_ELECTION";
                            case REFERENDUM -> "REFERENDUM_HELD";
                            case REGIONAL_ELECTION -> "REGIONAL_ELECTION_HELD";
                            case LOCAL_ELECTION -> "LOCAL_ELECTION_HELD";
                            case BY_ELECTION -> "BY_ELECTION_HELD";
                            case EXCLUDED -> throw new IllegalStateException("unreachable");
                        };

                        PoliticalEvent event = buildEvent(country, title, year, eventType);
                        politicalEventRepository.save(event);
                        inserted++;
                        log.info("Discovered {} for {}: {} ({})", eventType, country.getName(), title, event.getEventDate());
                    }
                }
            }

            run.setRecordsRead(read);
            finishRun(run, "SUCCESS", inserted, 0, skipped, null);
        } catch (Exception e) {
            log.error("Election discovery failed", e);
            run.setRecordsRead(read);
            finishRun(run, "FAILED", inserted, 0, skipped, e.getMessage());
        }

        return run;
    }

    private EventClassification classify(String title) {
        if (title.startsWith("Category:") || title.startsWith("Template:") || title.startsWith("Talk:")) {
            return EventClassification.EXCLUDED;
        }

        String lower = title.toLowerCase(Locale.ENGLISH);

        for (String pattern : LEADERSHIP_PATTERNS) {
            if (lower.contains(pattern)) {
                return EventClassification.LEADERSHIP_ELECTION;
            }
        }
        for (String pattern : BY_ELECTION_PATTERNS) {
            if (lower.contains(pattern)) {
                return EventClassification.BY_ELECTION;
            }
        }
        for (String pattern : LOCAL_PATTERNS) {
            if (lower.contains(pattern)) {
                return EventClassification.LOCAL_ELECTION;
            }
        }
        for (String pattern : REGIONAL_PATTERNS) {
            if (lower.contains(pattern)) {
                return EventClassification.REGIONAL_ELECTION;
            }
        }
        if (REFERENDUM_PATTERN.matcher(title).find()) {
            return EventClassification.REFERENDUM;
        }

        return EventClassification.NATIONAL_ELECTION;
    }

    private PoliticalEvent buildEvent(Country country, String articleTitle, int fallbackYear, String eventType) {
        PoliticalEvent event = new PoliticalEvent();
        event.setCountry(country);
        event.setSourceArticleTitle(articleTitle);
        event.setEventType(eventType);
        event.setTitle(articleTitle);

        String extract = safeFetchExtract(articleTitle);
        LocalDate date = extractDate(extract);
        if (date == null) {
            date = LocalDate.of(fallbackYear, 1, 1);
            event.setDescription("Exact date not determined from the article; year is known, day/month is not.");
        } else {
            event.setDescription(extract != null && extract.length() > 500 ? extract.substring(0, 500) : extract);
        }
        event.setEventDate(date);

        event.setSourceName("Wikipedia");
        event.setSourceUrl("https://en.wikipedia.org/wiki/" + articleTitle.replace(' ', '_'));

        LocalDateTime now = LocalDateTime.now();
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        return event;
    }

    private String safeFetchExtract(String articleTitle) {
        try {
            sleepBetweenCalls();
            return withRetryOnRateLimit(() -> client.fetchArticleExtract(articleTitle));
        } catch (Exception e) {
            log.warn("Extract fetch failed for {}: {}", articleTitle, e.getMessage());
            return null;
        }
    }

    private <T> T withRetryOnRateLimit(Supplier<T> call) {
        int attempt = 0;
        while (true) {
            try {
                return call.get();
            } catch (HttpClientErrorException.TooManyRequests e) {
                attempt++;
                if (attempt > MAX_RETRIES) {
                    throw e;
                }
                long backoff = BASE_BACKOFF_MS * attempt;
                log.warn("Rate limited, backing off {}ms (attempt {}/{})", backoff, attempt, MAX_RETRIES);
                sleep(backoff);
            } catch (ResourceAccessException e) {
                attempt++;
                if (attempt > MAX_RETRIES) {
                    throw e;
                }
                log.warn("Network error ({}), retrying in {}ms (attempt {}/{})", e.getMessage(), BASE_BACKOFF_MS, attempt, MAX_RETRIES);
                sleep(BASE_BACKOFF_MS);
            }
        }
    }

    private LocalDate extractDate(String text) {
        if (text == null) {
            return null;
        }
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return LocalDate.parse(matcher.group(1), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void sleepBetweenCalls() {
        sleep(DELAY_BETWEEN_CALLS_MS);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ImportRun startRun() {
        ImportRun run = new ImportRun();
        run.setSource(SOURCE);
        run.setStartedAt(LocalDateTime.now());
        run.setCreatedAt(LocalDateTime.now());
        run.setStatus("RUNNING");
        run.setRecordsRead(0);
        run.setRecordsInserted(0);
        run.setRecordsUpdated(0);
        run.setRecordsSkipped(0);
        return importRunRepository.save(run);
    }

    private void finishRun(ImportRun run, String status, int inserted, int updated, int skipped, String errorMessage) {
        run.setStatus(status);
        run.setFinishedAt(LocalDateTime.now());
        run.setRecordsInserted(inserted);
        run.setRecordsUpdated(updated);
        run.setRecordsSkipped(skipped);
        run.setErrorMessage(errorMessage);
        importRunRepository.save(run);
    }
}