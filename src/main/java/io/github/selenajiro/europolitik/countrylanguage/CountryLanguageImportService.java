package io.github.selenajiro.europolitik.countrylanguage;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.countrylanguage.CountryLanguageMapper.NormalizedLanguage;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import io.github.selenajiro.europolitik.wikidataimport.SparqlResultsDto;
import io.github.selenajiro.europolitik.wikidataimport.SparqlResultsDto.SparqlValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CountryLanguageImportService {

    private static final Logger log = LoggerFactory.getLogger(CountryLanguageImportService.class);
    private static final String SOURCE = "wikidata-languages";

    private final WikidataClientAdapter client;
    private final CountryRepository countryRepository;
    private final CountryLanguageRepository countryLanguageRepository;
    private final ImportRunRepository importRunRepository;

    public CountryLanguageImportService(io.github.selenajiro.europolitik.wikidataimport.WikidataClient wikidataClient,
                                        CountryRepository countryRepository,
                                        CountryLanguageRepository countryLanguageRepository,
                                        ImportRunRepository importRunRepository) {
        this.client = wikidataClient::queryLanguages;
        this.countryRepository = countryRepository;
        this.countryLanguageRepository = countryLanguageRepository;
        this.importRunRepository = importRunRepository;
    }

    @FunctionalInterface
    private interface WikidataClientAdapter {
        SparqlResultsDto queryLanguages();
    }

    @Transactional
    public ImportRun importLanguages() {
        ImportRun run = startRun();

        int inserted = 0;
        int updated = 0;
        int skipped = 0;
        int read = 0;

        try {
            SparqlResultsDto raw = client.queryLanguages();
            List<Map<String, SparqlValue>> bindings =
                    (raw == null || raw.results() == null) ? List.of() : raw.results().bindings();
            read = bindings.size();

            List<NormalizedLanguage> normalized = CountryLanguageMapper.normalize(bindings);
            List<Country> countries = countryRepository.findAll();

            Map<String, CountryLanguage> existingByKey = countryLanguageRepository.findAllWithCountry().stream()
                    .collect(Collectors.toMap(
                            l -> l.getCountry().getId() + "|" + l.getLanguage().toLowerCase(),
                            l -> l,
                            (a, b) -> a));

            for (NormalizedLanguage lang : normalized) {
                Optional<Country> countryOpt = countries.stream()
                        .filter(c -> lang.isoCode().equalsIgnoreCase(c.getIsoCode()))
                        .findFirst();
                if (countryOpt.isEmpty()) {
                    skipped++;
                    continue;
                }
                Country country = countryOpt.get();

                String key = country.getId() + "|" + lang.language().toLowerCase();
                CountryLanguage entity = existingByKey.getOrDefault(key, new CountryLanguage());
                boolean isNew = entity.getId() == null;
                boolean changed = CountryLanguageMapper.applyTo(entity, lang, country);

                if (isNew) {
                    countryLanguageRepository.save(entity);
                    inserted++;
                } else if (changed) {
                    countryLanguageRepository.save(entity);
                    updated++;
                } else {
                    skipped++;
                }
            }

            run.setRecordsRead(read);
            finishRun(run, "SUCCESS", inserted, updated, skipped, null);
        } catch (Exception e) {
            log.error("Language import failed", e);
            run.setRecordsRead(read);
            finishRun(run, "FAILED", inserted, updated, skipped, e.getMessage());
        }

        return run;
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
