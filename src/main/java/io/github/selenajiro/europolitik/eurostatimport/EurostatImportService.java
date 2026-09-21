package io.github.selenajiro.europolitik.eurostatimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.countrystatistic.CountryStatistic;
import io.github.selenajiro.europolitik.countrystatistic.CountryStatisticRepository;
import io.github.selenajiro.europolitik.eurostatimport.EurostatImportMapper.NormalizedStatistic;
import io.github.selenajiro.europolitik.eurostatimport.JsonStatDecoder.Observation;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class EurostatImportService {

    private static final Logger log = LoggerFactory.getLogger(EurostatImportService.class);
    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 5000;

    private final EurostatClient eurostatClient;
    private final CountryStatisticRepository countryStatisticRepository;
    private final CountryRepository countryRepository;
    private final ImportRunRepository importRunRepository;

    public EurostatImportService(EurostatClient eurostatClient,
                                 CountryStatisticRepository countryStatisticRepository,
                                 CountryRepository countryRepository,
                                 ImportRunRepository importRunRepository) {
        this.eurostatClient = eurostatClient;
        this.countryStatisticRepository = countryStatisticRepository;
        this.countryRepository = countryRepository;
        this.importRunRepository = importRunRepository;
    }

    @Transactional
    public ImportRun importIndicator(EurostatIndicatorSpec spec) {
        ImportRun run = startRun(spec);

        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        try {
            JsonStatDto raw = withRetry(() -> eurostatClient.fetch(spec));
            List<Observation> observations = JsonStatDecoder.decode(raw);
            run.setRecordsRead(observations.size());

            for (Observation observation : observations) {
                NormalizedStatistic normalized = EurostatImportMapper.normalize(observation);
                if (normalized == null) {
                    skipped++;
                    continue;
                }

                Optional<Country> country = countryRepository.findByIsoCode(normalized.isoCode());
                if (country.isEmpty()) {
                    skipped++;
                    continue;
                }

                Optional<CountryStatistic> existing = countryStatisticRepository
                        .findByCountryIdAndIndicatorAndYear(country.get().getId(), spec.indicatorCode(), normalized.year());
                CountryStatistic stat = existing.orElseGet(CountryStatistic::new);
                boolean isNew = existing.isEmpty();

                boolean changed = EurostatImportMapper.applyTo(stat, normalized, country.get(), spec);

                if (isNew) {
                    countryStatisticRepository.save(stat);
                    inserted++;
                } else if (changed) {
                    countryStatisticRepository.save(stat);
                    updated++;
                } else {
                    skipped++;
                }
            }

            finishRun(run, "SUCCESS", inserted, updated, skipped, null);
        } catch (Exception e) {
            log.error("Eurostat import failed for {}", spec.indicatorCode(), e);
            finishRun(run, "FAILED", inserted, updated, skipped, e.getMessage());
        }

        return run;
    }

    private <T> T withRetry(Supplier<T> call) {
        int attempt = 0;
        while (true) {
            try {
                return call.get();
            } catch (ResourceAccessException e) {
                attempt++;
                if (attempt > MAX_RETRIES) {
                    throw e;
                }
                long backoff = BASE_BACKOFF_MS * attempt;
                log.warn("Eurostat request failed ({}), retrying in {}ms (attempt {}/{})", e.getMessage(), backoff, attempt, MAX_RETRIES);
                sleep(backoff);
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ImportRun startRun(EurostatIndicatorSpec spec) {
        ImportRun run = new ImportRun();
        run.setSource("eurostat-" + spec.indicatorCode().toLowerCase());
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