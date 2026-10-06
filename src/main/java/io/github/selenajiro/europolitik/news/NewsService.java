package io.github.selenajiro.europolitik.news;

import io.github.selenajiro.europolitik.feed.FeedItem;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class NewsService {

    private final NewsArticleRepository newsArticleRepository;

    public NewsService(NewsArticleRepository newsArticleRepository) {
        this.newsArticleRepository = newsArticleRepository;
    }

    public NewsPageResponse buildPage(int offset, int limit, String search, Long countryId,
                                      LocalDate dateFrom, LocalDate dateTo) {
        List<NewsArticle> all = countryId != null
                ? newsArticleRepository.findAllByCountryId(countryId)
                : newsArticleRepository.findAllWithCountry();

        String searchLower = (search == null) ? null : search.toLowerCase(Locale.ENGLISH).trim();

        List<NewsArticle> filtered = all.stream()
                .filter(a -> dateFrom == null || !a.getPublishedDate().isBefore(dateFrom))
                .filter(a -> dateTo == null || !a.getPublishedDate().isAfter(dateTo))
                .filter(a -> searchLower == null || searchLower.isEmpty()
                        || a.getTitle().toLowerCase(Locale.ENGLISH).contains(searchLower))
                .sorted(Comparator.comparing(NewsArticle::getPublishedDate).reversed())
                .toList();

        int from = Math.min(offset, filtered.size());
        int to = Math.min(offset + limit, filtered.size());
        boolean hasMore = to < filtered.size();

        List<FeedItem> items = filtered.subList(from, to).stream().map(this::toFeedItem).toList();
        return new NewsPageResponse(items, hasMore);
    }

    private FeedItem toFeedItem(NewsArticle a) {
        return new FeedItem(
                "NEWS", a.getTitle(), a.getDescription(), a.getPublishedDate(),
                a.getCountry() != null ? a.getCountry().getId() : null,
                a.getCountry() != null ? a.getCountry().getName() : null,
                a.getCountry() != null ? a.getCountry().getIsoCode() : null,
                a.getSourceDomain(), a.getSourceUrl());
    }
}
