package io.github.selenajiro.europolitik.countrylanguage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CountryLanguageImportJob {

    private static final Logger log = LoggerFactory.getLogger(CountryLanguageImportJob.class);

    private final CountryLanguageImportService countryLanguageImportService;

    public CountryLanguageImportJob(CountryLanguageImportService countryLanguageImportService) {
        this.countryLanguageImportService = countryLanguageImportService;
    }

    @Scheduled(cron = "${europolitik.import.languages.cron:0 0 2 1 1 *}")
    public void run() {
        log.info("Starting Wikidata language import");
        countryLanguageImportService.importLanguages();
    }
}
