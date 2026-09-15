package io.github.selenajiro.europolitik.electionimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.election.ElectionRepository;
import io.github.selenajiro.europolitik.electionimport.ParlGovElectionImportMapper.NormalizedRow;
import io.github.selenajiro.europolitik.electionresult.ElectionResult;
import io.github.selenajiro.europolitik.electionresult.ElectionResultRepository;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import io.github.selenajiro.europolitik.politicalparty.PoliticalParty;
import io.github.selenajiro.europolitik.politicalparty.PoliticalPartyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ParlGovElectionImportService {

    private static final Logger log = LoggerFactory.getLogger(ParlGovElectionImportService.class);
    private static final String SOURCE = "parlgov-elections";

    private enum RowOutcome { INSERTED, UPDATED, UNCHANGED }

    private final ParlGovElectionImportClient client;
    private final CountryRepository countryRepository;
    private final PoliticalPartyRepository politicalPartyRepository;
    private final ElectionRepository electionRepository;
    private final ElectionResultRepository electionResultRepository;
    private final ImportRunRepository importRunRepository;
    private final TransactionTemplate transactionTemplate;

    public ParlGovElectionImportService(ParlGovElectionImportClient client,
                                        CountryRepository countryRepository,
                                        PoliticalPartyRepository politicalPartyRepository,
                                        ElectionRepository electionRepository,
                                        ElectionResultRepository electionResultRepository,
                                        ImportRunRepository importRunRepository,
                                        PlatformTransactionManager transactionManager) {
        this.client = client;
        this.countryRepository = countryRepository;
        this.politicalPartyRepository = politicalPartyRepository;
        this.electionRepository = electionRepository;
        this.electionResultRepository = electionResultRepository;
        this.importRunRepository = importRunRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public ImportRun importElections() {
        ImportRun run = startRun();

        int inserted = 0;
        int updated = 0;
        int skipped = 0;

        try {
            List<ParlGovElectionRow> rows = client.readRows();
            run.setRecordsRead(rows.size());

            Map<String, Optional<Country>> countryCache = new HashMap<>();

            for (ParlGovElectionRow raw : rows) {
                NormalizedRow normalized = ParlGovElectionImportMapper.normalize(raw);
                if (normalized == null) {
                    skipped++;
                    continue;
                }

                Country country = countryCache
                        .computeIfAbsent(normalized.countryIsoCode(), countryRepository::findByIsoCode)
                        .orElse(null);
                if (country == null) {
                    skipped++;
                    continue;
                }

                try {
                    RowOutcome outcome = transactionTemplate.execute(status -> processRow(normalized, country));
                    switch (outcome) {
                        case INSERTED -> inserted++;
                        case UPDATED -> updated++;
                        case UNCHANGED -> skipped++;
                    }
                } catch (Exception rowError) {
                    log.warn("Skipping row (election {} / party {}): {}",
                            normalized.parlgovElectionId(), normalized.parlgovPartyId(), rowError.getMessage());
                    skipped++;
                }
            }

            finishRun(run, "SUCCESS", inserted, updated, skipped, null);
        } catch (Exception e) {
            log.error("ParlGov election import failed", e);
            finishRun(run, "FAILED", inserted, updated, skipped, e.getMessage());
        }

        return run;
    }

    private RowOutcome processRow(NormalizedRow normalized, Country country) {
        PoliticalParty party = politicalPartyRepository.findByParlgovPartyId(normalized.parlgovPartyId())
                .orElseGet(PoliticalParty::new);
        if (ParlGovElectionImportMapper.applyToParty(party, normalized, country)) {
            party = politicalPartyRepository.save(party);
        }

        Election election = electionRepository.findByParlgovElectionId(normalized.parlgovElectionId())
                .orElseGet(Election::new);
        if (ParlGovElectionImportMapper.applyToElection(election, normalized, country)) {
            election = electionRepository.save(election);
        }

        Optional<ElectionResult> existingResult = electionResultRepository
                .findByElectionIdAndPartyId(election.getId(), party.getId());
        ElectionResult result = existingResult.orElseGet(ElectionResult::new);
        boolean isNew = existingResult.isEmpty();
        boolean changed = ParlGovElectionImportMapper.applyToResult(result, normalized, election, party);

        if (isNew) {
            electionResultRepository.save(result);
            return RowOutcome.INSERTED;
        } else if (changed) {
            electionResultRepository.save(result);
            return RowOutcome.UPDATED;
        }
        return RowOutcome.UNCHANGED;
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
