package io.github.selenajiro.europolitik.politicalleaning;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import io.github.selenajiro.europolitik.politicalparty.PoliticalParty;
import io.github.selenajiro.europolitik.politicalparty.PoliticalPartyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PoliticalLeaningService {

    private static final Logger log = LoggerFactory.getLogger(PoliticalLeaningService.class);
    private static final String SOURCE = "derived-political-leaning";

    private final CountryRepository countryRepository;
    private final PoliticalPartyRepository politicalPartyRepository;
    private final ImportRunRepository importRunRepository;

    public PoliticalLeaningService(CountryRepository countryRepository,
                                   PoliticalPartyRepository politicalPartyRepository,
                                   ImportRunRepository importRunRepository) {
        this.countryRepository = countryRepository;
        this.politicalPartyRepository = politicalPartyRepository;
        this.importRunRepository = importRunRepository;
    }

    @Transactional
    public ImportRun computeLeanings() {
        ImportRun run = startRun();

        int updated = 0;
        int skipped = 0;

        try {
            List<Country> countries = countryRepository.findAll();
            run.setRecordsRead(countries.size());

            for (Country country : countries) {
                String rulingParty = country.getCurrentRulingParty();
                if (rulingParty == null || rulingParty.isBlank()) {
                    skipped++;
                    continue;
                }

                List<PoliticalParty> parties = politicalPartyRepository.findAllByCountryId(country.getId());
                BigDecimal sum = BigDecimal.ZERO;
                int matches = 0;

                for (PoliticalParty party : parties) {
                    if (party.getLeftRight() == null) {
                        continue;
                    }
                    if (matchesText(rulingParty, party.getName()) || matchesText(rulingParty, party.getShortName())) {
                        sum = sum.add(party.getLeftRight());
                        matches++;
                    }
                }

                if (matches == 0) {
                    skipped++;
                    continue;
                }

                BigDecimal average = sum.divide(BigDecimal.valueOf(matches), 1, RoundingMode.HALF_UP);
                country.setCurrentPoliticalLeaning(average);
                country.setPoliticalLeaningComputedAt(LocalDateTime.now());
                countryRepository.save(country);
                updated++;
                log.info("Political leaning for {}: {} (from {} matched part{})",
                        country.getName(), average, matches, matches == 1 ? "y" : "ies");
            }

            finishRun(run, "SUCCESS", 0, updated, skipped, null);
        } catch (Exception e) {
            log.error("Political leaning computation failed", e);
            finishRun(run, "FAILED", 0, updated, skipped, e.getMessage());
        }

        return run;
    }

    private boolean matchesText(String haystack, String needle) {
        if (needle == null || needle.isBlank() || needle.length() < 3) {
            return false; // avoid matching tiny abbreviations against everything
        }
        return haystack.toLowerCase().contains(needle.toLowerCase());
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
