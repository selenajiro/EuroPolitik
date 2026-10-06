package io.github.selenajiro.europolitik.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EuNewsImportJob {

    private static final Logger log = LoggerFactory.getLogger(EuNewsImportJob.class);

    private final EuNewsImportService euNewsImportService;

    public EuNewsImportJob(EuNewsImportService euNewsImportService) {
        this.euNewsImportService = euNewsImportService;
    }

    @Scheduled(cron = "${europolitik.import.eu-rss.cron:0 0 * * * *}")
    public void run() {
        log.info("Starting scheduled EU Council RSS import");
        euNewsImportService.importNews();
    }
}
