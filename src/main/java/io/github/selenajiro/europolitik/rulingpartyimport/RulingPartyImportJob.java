package io.github.selenajiro.europolitik.rulingpartyimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RulingPartyImportJob {

    private static final Logger log = LoggerFactory.getLogger(RulingPartyImportJob.class);

    private final RulingPartyImportService rulingPartyImportService;

    public RulingPartyImportJob(RulingPartyImportService rulingPartyImportService) {
        this.rulingPartyImportService = rulingPartyImportService;
    }

    @Scheduled(cron = "${europolitik.import.wikipedia.ruling-party.cron:0 0 7 * * *}")
    public void run() {
        log.info("Starting scheduled ruling party import");
        rulingPartyImportService.importRulingParties();
    }
}
