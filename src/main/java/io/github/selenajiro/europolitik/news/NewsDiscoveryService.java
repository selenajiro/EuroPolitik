package io.github.selenajiro.europolitik.news;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

@Service
public class NewsDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(NewsDiscoveryService.class);
    private static final String SOURCE = "gdelt-news-discovery";
    private static final String PIPELINE = "GDELT";
    private static final long DELAY_BETWEEN_COUNTRIES_MS = 3000;
    private static final int MAX_RETRIES = 4;
    private static final long BASE_BACKOFF_MS = 5000;
    private static final int MAX_RECORDS_PER_COUNTRY = 75;

    // Deliberately broad -- same lesson as the original election-discovery
    // filter: better to include some borderline/adjacent stories than to
    // silently starve the data with an overly strict keyword list.
    private static final String KEYWORD_FILTER =
            "(government OR minister OR parliament OR election OR president OR coalition " +
                    "OR politics OR political OR policy OR legislation OR referendum OR chancellor " +
                    "OR \"prime minister\" OR cabinet OR opposition OR \"ruling party\" OR sanctions " +
                    "OR diplomat OR summit OR protest OR lawmaker OR senate OR congress OR vote OR ballot OR campaign)";

    // FIPS 10-4 codes, which GDELT requires -- different from the ISO codes
    // used everywhere else in this app for most of these. Verify against
    // real per-country counts after the first run.
    private static final Map<String, String> ISO_TO_FIPS = Map.ofEntries(
            Map.entry("AL", "AL"), Map.entry("AD", "AN"), Map.entry("AT", "AU"),
            Map.entry("BY", "BO"), Map.entry("BE", "BE"), Map.entry("BA", "BK"),
            Map.entry("BG", "BU"), Map.entry("HR", "HR"), Map.entry("CY", "CY"),
            Map.entry("CZ", "EZ"), Map.entry("DK", "DA"), Map.entry("EE", "EN"),
            Map.entry("FI", "FI"), Map.entry("FR", "FR"), Map.entry("DE", "GM"),
            Map.entry("GR", "GR"), Map.entry("HU", "HU"), Map.entry("IS", "IC"),
            Map.entry("IE", "EI"), Map.entry("IT", "IT"), Map.entry("XK", "KV"),
            Map.entry("LV", "LG"), Map.entry("LI", "LS"), Map.entry("LT", "LH"),
            Map.entry("LU", "LU"), Map.entry("MT", "MT"), Map.entry("MD", "MD"),
            Map.entry("MC", "MN"), Map.entry("ME", "MJ"), Map.entry("NL", "NL"),
            Map.entry("MK", "MK"), Map.entry("NO", "NO"), Map.entry("PL", "PL"),
            Map.entry("PT", "PO"), Map.entry("RO", "RO"), Map.entry("RU", "RS"),
            Map.entry("SM", "SM"), Map.entry("RS", "RI"), Map.entry("SK", "LO"),
            Map.entry("SI", "SI"), Map.entry("ES", "SP"), Map.entry("SE", "SW"),
            Map.entry("CH", "SZ"), Map.entry("TR", "TU"), Map.entry("UA", "UP"),
            Map.entry("GB", "UK"), Map.entry("VA", "VT")
    );

    private static final DateTimeFormatter SEENDATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);

    private final GdeltClient client;
    private final CountryRepository countryRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final ImportRunRepository importRunRepository;

    public NewsDiscoveryService(GdeltClient client,
                                CountryRepository countryRepository,
                                NewsArticleRepository newsArticleRepository,
                                ImportRunRepository importRunRepository) {
        this.client = client;
        this.countryRepository = countryRepository;
        this.newsArticleRepository = newsArticleRepository;
        this.importRunRepository = importRunRepository;
    }

    @Transactional
    public ImportRun discoverNews() {
        ImportRun run = startRun();

        int read = 0;
        int inserted = 0;
        int skipped = 0;

        try {
            List<Country> countries = countryRepository.findAll();
            boolean first = true;

            for (Country country : countries) {
                String fipsCode = ISO_TO_FIPS.get(country.getIsoCode());
                if (fipsCode == null) {
                    log.warn("No FIPS mapping for {} ({}), skipping", country.getName(), country.getIsoCode());
                    skipped++;
                    continue;
                }

                if (!first) {
                    sleepBetweenCalls();
                }
                first = false;

                GdeltResponseDto response;
                try {
                    response = withRetry(() -> client.searchNews(fipsCode, KEYWORD_FILTER, MAX_RECORDS_PER_COUNTRY));
                } catch (Exception e) {
                    log.warn("GDELT query failed for {}: {}", country.getName(), e.getMessage());
                    skipped++;
                    continue;
                }

                if (response == null || response.articles() == null) {
                    skipped++;
                    continue;
                }

                for (GdeltResponseDto.GdeltArticleDto article : response.articles()) {
                    read++;
                    if (article.url() == null || article.url().isBlank()
                            || article.title() == null || article.title().isBlank()) {
                        skipped++;
                        continue;
                    }

                    if (newsArticleRepository.findBySourceUrl(article.url()).isPresent()) {
                        skipped++;
                        continue;
                    }

                    LocalDate publishedDate = parseSeenDate(article.seendate());
                    if (publishedDate == null) {
                        publishedDate = LocalDate.now();
                    }

                    NewsArticle newsArticle = new NewsArticle();
                    newsArticle.setCountry(country);
                    newsArticle.setTitle(article.title().trim());
                    newsArticle.setSourceUrl(article.url());
                    newsArticle.setSourceDomain(article.domain());
                    newsArticle.setSourcePipeline(PIPELINE);
                    newsArticle.setPublishedDate(publishedDate);
                    newsArticle.setLanguage(article.language());
                    newsArticle.setImageUrl(article.socialimage());

                    LocalDateTime now = LocalDateTime.now();
                    newsArticle.setCreatedAt(now);
                    newsArticle.setUpdatedAt(now);

                    newsArticleRepository.save(newsArticle);
                    inserted++;
                }
            }

            run.setRecordsRead(read);
            finishRun(run, "SUCCESS", inserted, 0, skipped, null);
        } catch (Exception e) {
            log.error("News discovery failed", e);
            run.setRecordsRead(read);
            finishRun(run, "FAILED", inserted, 0, skipped, e.getMessage());
        }

        return run;
    }

    private LocalDate parseSeenDate(String seenDate) {
        if (seenDate == null || seenDate.isBlank()) {
            return null;
        }
        try {
            return java.time.LocalDateTime.parse(seenDate, SEENDATE_FORMAT).toLocalDate();
        } catch (Exception e) {
            return null;
        }
    }

    private <T> T withRetry(Supplier<T> call) {
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
                log.warn("GDELT rate limited, backing off {}ms (attempt {}/{})", backoff, attempt, MAX_RETRIES);
                sleep(backoff);
            } catch (ResourceAccessException e) {
                attempt++;
                if (attempt > MAX_RETRIES) {
                    throw e;
                }
                log.warn("GDELT network error ({}), retrying in {}ms (attempt {}/{})", e.getMessage(), BASE_BACKOFF_MS, attempt, MAX_RETRIES);
                sleep(BASE_BACKOFF_MS);
            }
        }
    }

    private void sleepBetweenCalls() {
        sleep(DELAY_BETWEEN_COUNTRIES_MS);
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