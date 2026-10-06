package io.github.selenajiro.europolitik.feed;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.election.ElectionRepository;
import io.github.selenajiro.europolitik.favoritecountry.FavoriteCountry;
import io.github.selenajiro.europolitik.favoritecountry.FavoriteCountryRepository;
import io.github.selenajiro.europolitik.news.NewsArticle;
import io.github.selenajiro.europolitik.news.NewsArticleRepository;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEvent;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventRepository;
import io.github.selenajiro.europolitik.user.UserAccount;
import io.github.selenajiro.europolitik.user.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PersonalizedFeedService {

    private static final int FAVORITE_BUFFER_DAYS = 60;
    private static final int SEEN_GRACE_HOURS = 24;
    private static final String COMPOSITION_TYPE = "CURRENT_COMPOSITION";
    private static final int SNIPPET_LENGTH = 180;

    private final FavoriteCountryRepository favoriteCountryRepository;
    private final PoliticalEventRepository politicalEventRepository;
    private final ElectionRepository electionRepository;
    private final NewsArticleRepository newsArticleRepository;
    private final UserAccountRepository userAccountRepository;
    private final FeedSeenItemRepository feedSeenItemRepository;

    public PersonalizedFeedService(FavoriteCountryRepository favoriteCountryRepository,
                                   PoliticalEventRepository politicalEventRepository,
                                   ElectionRepository electionRepository,
                                   NewsArticleRepository newsArticleRepository,
                                   UserAccountRepository userAccountRepository,
                                   FeedSeenItemRepository feedSeenItemRepository) {
        this.favoriteCountryRepository = favoriteCountryRepository;
        this.politicalEventRepository = politicalEventRepository;
        this.electionRepository = electionRepository;
        this.newsArticleRepository = newsArticleRepository;
        this.userAccountRepository = userAccountRepository;
        this.feedSeenItemRepository = feedSeenItemRepository;
    }

    @Transactional
    public PersonalizedFeedResponse buildPage(String username, int offset, int limit) {
        UserAccount user = userAccountRepository.findByUsername(username).orElseThrow();
        List<FavoriteCountry> favorites = favoriteCountryRepository.findAllByUsername(username);

        LocalDateTime now = LocalDateTime.now();
        List<FeedSeenItem> seenRows = feedSeenItemRepository.findAllByUserId(user.getId());

        Set<String> permanentlySeenKeys = seenRows.stream()
                .filter(s -> s.getFirstShownAt().isBefore(now.minusHours(SEEN_GRACE_HOURS)))
                .map(FeedSeenItem::getItemKey)
                .collect(Collectors.toSet());
        Set<String> alreadyTrackedKeys = seenRows.stream()
                .map(FeedSeenItem::getItemKey)
                .collect(Collectors.toSet());

        List<ScoredItem> candidates = new ArrayList<>();

        for (NewsArticle article : newsArticleRepository.findAllWithNoCountry()) {
            String key = "NEWS:" + article.getId();
            if (permanentlySeenKeys.contains(key)) {
                continue;
            }
            candidates.add(new ScoredItem(key, article.getPublishedDate(), toFeedItem(article)));
        }

        for (FavoriteCountry favorite : favorites) {
            Country country = favorite.getCountry();
            LocalDate cutoff = favorite.getCreatedAt().toLocalDate().minusDays(FAVORITE_BUFFER_DAYS);

            for (PoliticalEvent event : politicalEventRepository.findAllByCountryId(country.getId())) {
                if (event.getEventDate() == null || event.getEventDate().isBefore(cutoff)) {
                    continue;
                }
                String key = "EVENT:" + event.getId();
                if (permanentlySeenKeys.contains(key)) {
                    continue;
                }
                candidates.add(new ScoredItem(key, event.getEventDate(), toFeedItem(event)));
            }

            for (Election election : electionRepository.findAllByCountryId(country.getId())) {
                if (COMPOSITION_TYPE.equals(election.getType())
                        || election.getElectionDate() == null
                        || election.getElectionDate().isBefore(cutoff)) {
                    continue;
                }
                String key = "ELECTION:" + election.getId();
                if (permanentlySeenKeys.contains(key)) {
                    continue;
                }
                candidates.add(new ScoredItem(key, election.getElectionDate(), toFeedItem(election)));
            }

            for (NewsArticle article : newsArticleRepository.findAllByCountryId(country.getId())) {
                if (article.getPublishedDate() == null || article.getPublishedDate().isBefore(cutoff)) {
                    continue;
                }
                String key = "NEWS:" + article.getId();
                if (permanentlySeenKeys.contains(key)) {
                    continue;
                }
                candidates.add(new ScoredItem(key, article.getPublishedDate(), toFeedItem(article)));
            }
        }

        candidates.sort(Comparator.comparing(ScoredItem::date).reversed());

        int from = Math.min(offset, candidates.size());
        int to = Math.min(offset + limit, candidates.size());
        List<ScoredItem> page = candidates.subList(from, to);
        boolean hasMore = to < candidates.size();

        for (ScoredItem item : page) {
            if (!alreadyTrackedKeys.contains(item.key())) {
                FeedSeenItem seen = new FeedSeenItem();
                seen.setUser(user);
                seen.setItemKey(item.key());
                seen.setFirstShownAt(now);
                feedSeenItemRepository.save(seen);
            }
        }

        return new PersonalizedFeedResponse(page.stream().map(ScoredItem::item).toList(), hasMore);
    }

    private record ScoredItem(String key, LocalDate date, FeedItem item) {}

    private FeedItem toFeedItem(PoliticalEvent e) {
        return new FeedItem(
                e.getEventType(), e.getTitle(), snippet(e.getDescription()), e.getEventDate(),
                e.getCountry() != null ? e.getCountry().getId() : null,
                e.getCountry() != null ? e.getCountry().getName() : null,
                e.getCountry() != null ? e.getCountry().getIsoCode() : null,
                e.getSourceName(), e.getSourceUrl());
    }

    private FeedItem toFeedItem(Election e) {
        return new FeedItem(
                "ELECTION", e.getName(), null, e.getElectionDate(),
                e.getCountry().getId(), e.getCountry().getName(), e.getCountry().getIsoCode(),
                e.getSourceName(), e.getSourceUrl());
    }

    private FeedItem toFeedItem(NewsArticle a) {
        return new FeedItem(
                "NEWS", a.getTitle(), snippet(a.getDescription()), a.getPublishedDate(),
                a.getCountry() != null ? a.getCountry().getId() : null,
                a.getCountry() != null ? a.getCountry().getName() : null,
                a.getCountry() != null ? a.getCountry().getIsoCode() : null,
                a.getSourceDomain(), a.getSourceUrl());
    }

    private String snippet(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String trimmed = description.strip();
        return trimmed.length() > SNIPPET_LENGTH ? trimmed.substring(0, SNIPPET_LENGTH).strip() + "…" : trimmed;
    }
}
