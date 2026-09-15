package io.github.selenajiro.europolitik.electionimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ParlGovElectionImportJob {

    private static final Logger log = LoggerFactory.getLogger(ParlGovElectionImportJob.class);

    private final ParlGovElectionImportService parlGovElectionImportService;

    public ParlGovElectionImportJob(ParlGovElectionImportService parlGovElectionImportService) {
        this.parlGovElectionImportService = parlGovElectionImportService;
    }

    @Scheduled(cron = "${europolitik.import.parlgov.cron:0 0 5 1 * *}")
    public void run() {
        log.info("Starting scheduled ParlGov election import");
        parlGovElectionImportService.importElections();
    }
}
