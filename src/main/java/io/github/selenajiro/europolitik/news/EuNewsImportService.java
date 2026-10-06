package io.github.selenajiro.europolitik.news;

import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EuNewsImportService {

    private static final Logger log = LoggerFactory.getLogger(EuNewsImportService.class);
    private static final String SOURCE = "eu-council-rss";
    private static final String PIPELINE = "EU_COUNCIL_RSS";

    private final EuRssClient client;
    private final NewsArticleRepository newsArticleRepository;
    private final ImportRunRepository importRunRepository;

    public EuNewsImportService(EuRssClient client,
                               NewsArticleRepository newsArticleRepository,
                               ImportRunRepository importRunRepository) {
        this.client = client;
        this.newsArticleRepository = newsArticleRepository;
        this.importRunRepository = importRunRepository;
    }

    @Transactional
    public ImportRun importNews() {
        ImportRun run = startRun();

        int read = 0;
        int inserted = 0;
        int skipped = 0;

        try {
            String raw = client.fetchRaw();
            List<EuRssParser.RssItem> items = EuRssParser.parse(raw);
            read = items.size();

            for (EuRssParser.RssItem item : items) {
                if (item.link().length() > 1000) {
                    skipped++;
                    continue;
                }

                if (newsArticleRepository.findBySourceUrl(item.link()).isPresent()) {
                    skipped++;
                    continue;
                }

                NewsArticle newsArticle = new NewsArticle();
                newsArticle.setCountry(null);
                newsArticle.setTitle(truncate(item.title(), 500));
                newsArticle.setDescription(truncate(item.description(), 1000));
                newsArticle.setSourceUrl(item.link());
                newsArticle.setSourceDomain(hostOf(item.link()));
                newsArticle.setSourcePipeline(PIPELINE);
                newsArticle.setPublishedDate(item.publishedDate());
                newsArticle.setLanguage("English");

                LocalDateTime now = LocalDateTime.now();
                newsArticle.setCreatedAt(now);
                newsArticle.setUpdatedAt(now);

                newsArticleRepository.save(newsArticle);
                inserted++;
            }

            run.setRecordsRead(read);
            finishRun(run, "SUCCESS", inserted, 0, skipped, null);
        } catch (Exception e) {
            log.error("EU Council RSS import failed", e);
            run.setRecordsRead(read);
            finishRun(run, "FAILED", inserted, 0, skipped, e.getMessage());
        }

        return run;
    }

    private String hostOf(String url) {
        try {
            return URI.create(url).getHost();
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }

    private ImportRun startRun() {
        ImportRun run = new ImportRun();
        run.setSource(SOURCE);
        run.setStartedAt(LocalDateTime.now());
        run.setCreatedAt(LocalDateTime.now());
        run.setStatus("RUNNING");
        run.setRecordsRead(0);
        run.setRecordsInserted(0);
        run.setRecordsUpdated(0);
        run.setRecordsSkipped(0);
        return importRunRepository.save(run);
    }

    private void finishRun(ImportRun run, String status, int inserted, int updated, int skipped, String errorMessage) {
        run.setStatus(status);
        run.setFinishedAt(LocalDateTime.now());
        run.setRecordsInserted(inserted);
        run.setRecordsUpdated(updated);
        run.setRecordsSkipped(skipped);
        run.setErrorMessage(errorMessage);
        importRunRepository.save(run);
    }
}
