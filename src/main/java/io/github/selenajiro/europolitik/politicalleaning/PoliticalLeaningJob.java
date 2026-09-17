package io.github.selenajiro.europolitik.politicalleaning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PoliticalLeaningJob {

    private static final Logger log = LoggerFactory.getLogger(PoliticalLeaningJob.class);

    private final PoliticalLeaningService politicalLeaningService;

    public PoliticalLeaningJob(PoliticalLeaningService politicalLeaningService) {
        this.politicalLeaningService = politicalLeaningService;
    }

    @Scheduled(cron = "${europolitik.import.political-leaning.cron:0 30 7 * * *}")
    public void run() {
        log.info("Starting political leaning computation");
        politicalLeaningService.computeLeanings();
    }
}
