package io.github.selenajiro.europolitik.mepimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import io.github.selenajiro.europolitik.mep.Mep;
import io.github.selenajiro.europolitik.mep.MepRepository;
import io.github.selenajiro.europolitik.mepimport.MepImportMapper.NormalizedMep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MepImportService {

    private static final Logger log = LoggerFactory.getLogger(MepImportService.class);
    private static final String SOURCE = "european-parliament-meps";

    private final MepImportClient mepImportClient;
    private final MepRepository mepRepository;
    private final CountryRepository countryRepository;
    private final ImportRunRepository importRunRepository;

    public MepImportService(MepImportClient mepImportClient,
                            MepRepository mepRepository,
                            CountryRepository countryRepository,
                            ImportRunRepository importRunRepository) {
        this.mepImportClient = mepImportClient;
        this.mepRepository = mepRepository;
        this.countryRepository = countryRepository;
        this.importRunRepository = importRunRepository;
    }

    @Transactional
    public ImportRun importCurrentMeps() {
        ImportRun run = startRun();

        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        try {
            List<EpMepDto> raw = mepImportClient.fetchCurrentMeps();
            run.setRecordsRead(raw.size());

            for (EpMepDto dto : raw) {
                NormalizedMep normalized = MepImportMapper.normalize(dto);
                if (normalized == null) {
                    skipped++;
                    continue;
                }

                Optional<Country> country = countryRepository.findByIsoCode(normalized.isoCode());
                if (country.isEmpty()) {
                    log.warn("Skipping MEP {} - unknown country code '{}'", normalized.epMemberId(), normalized.isoCode());
                    skipped++;
                    continue;
                }

                Optional<Mep> existing = mepRepository.findByEpMemberId(normalized.epMemberId());
                Mep mep = existing.orElseGet(Mep::new);
                boolean isNew = existing.isEmpty();

                boolean changed = MepImportMapper.applyTo(mep, normalized, country.get());

                if (isNew) {
                    mepRepository.save(mep);
                    inserted++;
                    publishChangeEvent(mep, "CREATED");
                } else if (changed) {
                    mepRepository.save(mep);
                    updated++;
                    publishChangeEvent(mep, "UPDATED");
                } else {
                    skipped++;
                }
            }

            finishRun(run, "SUCCESS", inserted, updated, skipped, null);
        } catch (Exception e) {
            log.error("MEP import failed", e);
            finishRun(run, "FAILED", inserted, updated, skipped, e.getMessage());
        }

        return run;
    }

    // Placeholder seam for the WebSocket layer: once STOMP is wired up,
    // this publishes a domain event instead of just logging.
    private void publishChangeEvent(Mep mep, String kind) {
        log.debug("MEP {} ({}): {}", mep.getFullName(), mep.getCountry().getIsoCode(), kind);
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
