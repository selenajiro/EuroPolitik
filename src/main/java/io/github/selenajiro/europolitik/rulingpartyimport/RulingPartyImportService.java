package io.github.selenajiro.europolitik.rulingpartyimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEvent;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class RulingPartyImportService {

    private static final Logger log = LoggerFactory.getLogger(RulingPartyImportService.class);
    private static final String SOURCE = "wikipedia-ruling-party";
    private static final String SOURCE_URL = "https://en.wikipedia.org/wiki/List_of_ruling_political_parties_by_country";

    private static final Map<String, String> NAME_ALIASES = Map.of(
            "Czech Republic", "Czechia"
    );

    private final WikipediaClient client;
    private final CountryRepository countryRepository;
    private final PoliticalEventRepository politicalEventRepository;
    private final ImportRunRepository importRunRepository;

    public RulingPartyImportService(WikipediaClient client,
                                    CountryRepository countryRepository,
                                    PoliticalEventRepository politicalEventRepository,
                                    ImportRunRepository importRunRepository) {
        this.client = client;
        this.countryRepository = countryRepository;
        this.politicalEventRepository = politicalEventRepository;
        this.importRunRepository = importRunRepository;
    }

    @Transactional
    public ImportRun importRulingParties() {
        ImportRun run = startRun();

        int updated = 0;
        int skipped = 0;

        try {
            String wikitext = client.fetchRulingPartiesWikitext();
            Map<String, String> byCountryName = RulingPartyWikitextParser.parse(wikitext);
            run.setRecordsRead(byCountryName.size());

            for (Map.Entry<String, String> entry : byCountryName.entrySet()) {
                String wikipediaName = entry.getKey();
                String lookupName = NAME_ALIASES.getOrDefault(wikipediaName, wikipediaName);

                Optional<Country> countryOpt = countryRepository.findByNameIgnoreCase(lookupName);
                if (countryOpt.isEmpty()) {
                    continue;
                }

                Country country = countryOpt.get();
                String rulingParty = entry.getValue();
                String previousRulingParty = country.getCurrentRulingParty();

                if (rulingParty.equals(previousRulingParty)) {
                    skipped++;
                    continue;
                }

                boolean isRealChange = previousRulingParty != null && !previousRulingParty.isBlank();

                country.setCurrentRulingParty(rulingParty);
                country.setRulingPartySourceName("Wikipedia");
                country.setRulingPartySourceUrl(SOURCE_URL);
                country.setRulingPartyUpdatedAt(LocalDateTime.now());
                countryRepository.save(country);
                updated++;
                log.info("Ruling party for {}: {}", country.getName(), rulingParty);

                if (isRealChange) {
                    logGovernmentChangeEvent(country, previousRulingParty, rulingParty);
                }
            }

            finishRun(run, "SUCCESS", 0, updated, skipped, null);
        } catch (Exception e) {
            log.error("Ruling party import failed", e);
            finishRun(run, "FAILED", 0, updated, skipped, e.getMessage());
        }

        return run;
    }

    private void logGovernmentChangeEvent(Country country, String previous, String current) {
        PoliticalEvent event = new PoliticalEvent();
        event.setCountry(country);
        event.setEventType("GOVERNMENT_CHANGE");
        event.setTitle("New ruling party/coalition in " + country.getName());
        event.setDescription("Previously: " + previous + "\nNow: " + current);
        event.setEventDate(LocalDate.now());
        event.setSourceName("Wikipedia");
        event.setSourceUrl(SOURCE_URL);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        politicalEventRepository.save(event);
        log.info("Logged government change event for {}", country.getName());
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
