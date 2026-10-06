package io.github.selenajiro.europolitik.news;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping("/api/news")
    public NewsPageResponse news(@RequestParam(defaultValue = "0") int offset,
                                 @RequestParam(defaultValue = "20") int limit,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) Long countryId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return newsService.buildPage(offset, Math.min(limit, 100), search, countryId, dateFrom, dateTo);
    }
}
