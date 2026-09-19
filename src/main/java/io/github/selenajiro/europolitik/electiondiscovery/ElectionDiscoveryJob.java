package io.github.selenajiro.europolitik.electiondiscovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Elections can happen any day, so this checks daily. */
@Component
public class ElectionDiscoveryJob {

    private static final Logger log = LoggerFactory.getLogger(ElectionDiscoveryJob.class);

    private final ElectionDiscoveryService electionDiscoveryService;

    public ElectionDiscoveryJob(ElectionDiscoveryService electionDiscoveryService) {
        this.electionDiscoveryService = electionDiscoveryService;
    }

    @Scheduled(cron = "${europolitik.import.election-discovery.cron:0 0 8 * * *}")
    public void run() {
        log.info("Starting scheduled election discovery");
        electionDiscoveryService.discoverElections();
    }
}
