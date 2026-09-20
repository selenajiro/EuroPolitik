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

/**
 * Discovers recent/current NATIONAL-level elections per country via
 * Wikipedia's "Category:{year} elections in {country}" categories.
 * <p>
 * These categories include far more than national elections -- regional
 * legislature races, city/municipal elections, party leadership
 * primaries, by-elections, and even MediaWiki Category:/Template: pages
 * that aren't real articles at all. A title is only accepted if it
 * contains the country's own demonym (so region-named sub-national
 * elections, which usually don't mention the country at all, drop out)
 * AND doesn't match a known list of sub-national/internal-party
 * patterns (so country-wide local/municipal elections, which DO
 * mention the country, still get excluded).
 */
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

    // Demonym used to confirm a title is actually about this country,
    // not a region within it. Article titles for sub-national elections
    // (e.g. "Baden-Württemberg state election") typically don't include
    // the country name/demonym at all.
    private static final Map<String, String> DEMONYMS = Map.ofEntries(
            Map.entry("Albania", "Albanian"), Map.entry("Andorra", "Andorran"),
            Map.entry("Austria", "Austrian"), Map.entry("Belarus", "Belarusian"),
            Map.entry("Belgium", "Belgian"), Map.entry("Bosnia and Herzegovina", "Bosnian"),
            Map.entry("Bulgaria", "Bulgarian"), Map.entry("Croatia", "Croatian"),
            Map.entry("Cyprus", "Cypriot"), Map.entry("Czechia", "Czech"),
            Map.entry("Denmark", "Danish"), Map.entry("Estonia", "Estonian"),
            Map.entry("Finland", "Finnish"), Map.entry("France", "French"),
            Map.entry("Germany", "German"), Map.entry("Greece", "Greek"),
            Map.entry("Hungary", "Hungarian"), Map.entry("Iceland", "Icelandic"),
            Map.entry("Ireland", "Irish"), Map.entry("Italy", "Italian"),
            Map.entry("Kosovo", "Kosovar"), Map.entry("Latvia", "Latvian"),
            Map.entry("Liechtenstein", "Liechtenstein"), Map.entry("Lithuania", "Lithuanian"),
            Map.entry("Luxembourg", "Luxembourg"), Map.entry("Malta", "Maltese"),
            Map.entry("Moldova", "Moldovan"), Map.entry("Monaco", "Monaco"),
            Map.entry("Montenegro", "Montenegrin"), Map.entry("Netherlands", "Dutch"),
            Map.entry("North Macedonia", "Macedonian"), Map.entry("Norway", "Norwegian"),
            Map.entry("Poland", "Polish"), Map.entry("Portugal", "Portuguese"),
            Map.entry("Romania", "Romanian"), Map.entry("Russia", "Russian"),
            Map.entry("San Marino", "Sammarinese"), Map.entry("Serbia", "Serbian"),
            Map.entry("Slovakia", "Slovak"), Map.entry("Slovenia", "Slovenian"),
            Map.entry("Spain", "Spanish"), Map.entry("Sweden", "Swedish"),
            Map.entry("Switzerland", "Swiss"), Map.entry("Turkey", "Turkish"),
            Map.entry("Ukraine", "Ukrainian"), Map.entry("United Kingdom", "British"),
            Map.entry("Vatican City", "Vatican")
    );

    private static final List<String> NOISE_PATTERNS = List.of(
            "local election", "municipal", "primary", "leadership election",
            "by-election", "by election", "consular", "state election",
            "gubernatorial", "head election", "regional election", "senate election",
            "council election", "district election"
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(\\d{1,2}\\s+(?:January|February|March|April|May|June|July|August|September|October|November|December)\\s+\\d{4})\\b");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

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
        int year = LocalDate.now().getYear();

        try {
            List<Country> countries = countryRepository.findAll();

            for (Country country : countries) {
                String categoryCountryName = CATEGORY_NAME_ALIASES.getOrDefault(country.getName(), country.getName());
                String demonym = DEMONYMS.get(country.getName());

                List<String> titles;
                try {
                    sleepBetweenCalls();
                    titles = withRetryOnRateLimit(() -> client.fetchElectionCategoryMembers(categoryCountryName, year));
                } catch (Exception e) {
                    log.warn("Category lookup failed for {}: {}", country.getName(), e.getMessage());
                    skipped++;
                    continue;
                }

                for (String title : titles) {
                    read++;
                    if (!isNationalLevelElection(title, country.getName(), demonym)) {
                        skipped++;
                        continue;
                    }

                    if (politicalEventRepository.findBySourceArticleTitle(title).isPresent()) {
                        skipped++;
                        continue;
                    }

                    PoliticalEvent event = buildEvent(country, title, year);
                    politicalEventRepository.save(event);
                    inserted++;
                    log.info("Discovered election for {}: {} ({})", country.getName(), title, event.getEventDate());
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

    private boolean isNationalLevelElection(String title, String countryName, String demonym) {
        if (title.startsWith("Category:") || title.startsWith("Template:") || title.startsWith("Talk:")) {
            return false;
        }

        boolean mentionsCountry = (demonym != null && title.contains(demonym)) || title.contains(countryName);
        if (!mentionsCountry) {
            return false;
        }

        String lower = title.toLowerCase(Locale.ENGLISH);
        for (String noise : NOISE_PATTERNS) {
            if (lower.contains(noise)) {
                return false;
            }
        }

        return true;
    }

    private PoliticalEvent buildEvent(Country country, String articleTitle, int fallbackYear) {
        PoliticalEvent event = new PoliticalEvent();
        event.setCountry(country);
        event.setSourceArticleTitle(articleTitle);
        event.setEventType("ELECTION_HELD");
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