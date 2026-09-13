package io.github.selenajiro.europolitik.mepimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MepImportJob {

    private static final Logger log = LoggerFactory.getLogger(MepImportJob.class);

    private final MepImportService mepImportService;

    public MepImportJob(MepImportService mepImportService) {
        this.mepImportService = mepImportService;
    }

    @Scheduled(cron = "${europolitik.import.ep.mep.cron:0 0 */6 * * *}")
    public void run() {
        log.info("Starting scheduled MEP import");
        mepImportService.importCurrentMeps();
    }
}
