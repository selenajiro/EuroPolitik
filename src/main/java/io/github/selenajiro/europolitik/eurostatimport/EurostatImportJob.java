package io.github.selenajiro.europolitik.eurostatimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EurostatImportJob {

    private static final Logger log = LoggerFactory.getLogger(EurostatImportJob.class);

    private final EurostatImportService eurostatImportService;

    public EurostatImportJob(EurostatImportService eurostatImportService) {
        this.eurostatImportService = eurostatImportService;
    }

    @Scheduled(cron = "${europolitik.import.eurostat.population.cron:0 0 3 * * MON}")
    public void run() {
        log.info("Starting scheduled Eurostat population import");
        eurostatImportService.importPopulation();
    }
}