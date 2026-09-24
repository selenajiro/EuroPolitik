package io.github.selenajiro.europolitik.news;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NewsDiscoveryJob {

    private static final Logger log = LoggerFactory.getLogger(NewsDiscoveryJob.class);

    private final NewsDiscoveryService newsDiscoveryService;

    public NewsDiscoveryJob(NewsDiscoveryService newsDiscoveryService) {
        this.newsDiscoveryService = newsDiscoveryService;
    }

    @Scheduled(cron = "${europolitik.import.news.cron:0 0 */4 * * *}")
    public void run() {
        log.info("Starting scheduled GDELT news discovery");
        newsDiscoveryService.discoverNews();
    }
}