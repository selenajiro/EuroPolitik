package io.github.selenajiro.europolitik.wikidataimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WikidataCompositionImportJob {

    private static final Logger log = LoggerFactory.getLogger(WikidataCompositionImportJob.class);

    private final WikidataCompositionImportService wikidataCompositionImportService;

    public WikidataCompositionImportJob(WikidataCompositionImportService wikidataCompositionImportService) {
        this.wikidataCompositionImportService = wikidataCompositionImportService;
    }

    @Scheduled(cron = "${europolitik.import.wikidata.composition.cron:0 0 6 * * *}")
    public void run() {
        log.info("Starting scheduled Wikidata composition import");
        wikidataCompositionImportService.importCurrentComposition();
    }
}
