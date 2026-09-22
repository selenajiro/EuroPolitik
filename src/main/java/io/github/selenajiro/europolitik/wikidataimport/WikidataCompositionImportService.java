package io.github.selenajiro.europolitik.wikidataimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.election.ElectionRepository;
import io.github.selenajiro.europolitik.electionresult.ElectionResult;
import io.github.selenajiro.europolitik.electionresult.ElectionResultRepository;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import io.github.selenajiro.europolitik.politicalparty.PoliticalParty;
import io.github.selenajiro.europolitik.politicalparty.PoliticalPartyRepository;
import io.github.selenajiro.europolitik.wikidataimport.SparqlResultsDto.SparqlValue;
import io.github.selenajiro.europolitik.wikidataimport.WikidataCompositionMapper.NormalizedPartyComposition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WikidataCompositionImportService {

    private static final Logger log = LoggerFactory.getLogger(WikidataCompositionImportService.class);
    private static final String SOURCE = "wikidata-current-composition";
    private static final long DELAY_BETWEEN_COUNTRIES_MS = 500;

    private enum RowOutcome { INSERTED, UPDATED, UNCHANGED }

    private final WikidataClient client;
    private final CountryRepository countryRepository;
    private final PoliticalPartyRepository politicalPartyRepository;
    private final ElectionRepository electionRepository;
    private final ElectionResultRepository electionResultRepository;
    private final ImportRunRepository importRunRepository;
    private final TransactionTemplate transactionTemplate;

    public WikidataCompositionImportService(WikidataClient client,
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

    public ImportRun importCurrentComposition() {
        ImportRun run = startRun();

        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int read = 0;

        try {
            boolean first = true;
            for (WikidataLegislatureSpec spec : WikidataLegislatures.SPECS) {
                if (!first) {
                    sleepBetweenCountries();
                }
                first = false;

                Optional<Country> countryOpt = countryRepository.findByIsoCode(spec.countryIsoCode());
                if (countryOpt.isEmpty()) {
                    log.warn("Skipping Wikidata composition for {} - country not found", spec.countryIsoCode());
                    skipped++;
                    continue;
                }
                Country country = countryOpt.get();

                List<Map<String, SparqlValue>> bindings;
                try {
                    SparqlResultsDto raw = client.queryCurrentComposition(spec.positionQid());
                    bindings = (raw == null || raw.results() == null) ? List.of() : raw.results().bindings();
                } catch (Exception queryError) {
                    log.warn("Wikidata query failed for {}: {}", spec.countryIsoCode(), queryError.getMessage());
                    skipped++;
                    continue;
                }

                if (bindings.isEmpty()) {
                    log.warn("No current-composition data from Wikidata for {}", spec.countryIsoCode());
                    skipped++;
                    continue;
                }

                for (Map<String, SparqlValue> binding : bindings) {
                    read++;
                    NormalizedPartyComposition normalized = WikidataCompositionMapper.normalize(binding, spec.countryIsoCode());
                    if (normalized == null) {
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
                        log.warn("Skipping Wikidata party {} in {}: {}",
                                normalized.wikidataPartyId(), spec.countryIsoCode(), rowError.getMessage());
                        skipped++;
                    }
                }
            }

            run.setRecordsRead(read);
            finishRun(run, "SUCCESS", inserted, updated, skipped, null);
        } catch (Exception e) {
            log.error("Wikidata composition import failed", e);
            run.setRecordsRead(read);
            finishRun(run, "FAILED", inserted, updated, skipped, e.getMessage());
        }

        return run;
    }

    private void sleepBetweenCountries() {
        try {
            Thread.sleep(DELAY_BETWEEN_COUNTRIES_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private RowOutcome processRow(NormalizedPartyComposition normalized, Country country) {
        PoliticalParty party = politicalPartyRepository.findByWikidataPartyId(normalized.wikidataPartyId())
                .or(() -> findExistingPartyByName(normalized.partyLabel(), country))
                .orElseGet(PoliticalParty::new);
        if (WikidataCompositionMapper.applyToParty(party, normalized, country)) {
            party = politicalPartyRepository.save(party);
        }

        Election election = electionRepository.findByCountryIdAndType(country.getId(), "CURRENT_COMPOSITION")
                .orElseGet(Election::new);
        WikidataCompositionMapper.applyToElection(election, country);
        election = electionRepository.save(election);

        Optional<ElectionResult> existingResult = electionResultRepository
                .findByElectionIdAndPartyId(election.getId(), party.getId());
        ElectionResult result = existingResult.orElseGet(ElectionResult::new);
        boolean isNew = existingResult.isEmpty();
        boolean changed = WikidataCompositionMapper.applyToResult(result, normalized, election, party);

        if (isNew) {
            electionResultRepository.save(result);
            return RowOutcome.INSERTED;
        } else if (changed) {
            electionResultRepository.save(result);
            return RowOutcome.UPDATED;
        }
        return RowOutcome.UNCHANGED;
    }

    private Optional<PoliticalParty> findExistingPartyByName(String label, Country country) {
        return politicalPartyRepository.findAllByCountryId(country.getId()).stream()
                .filter(p -> p.getWikidataPartyId() == null)
                .filter(p -> WikidataCompositionMapper.namesMatch(label, p.getName(), country.getName()))
                .findFirst();
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
