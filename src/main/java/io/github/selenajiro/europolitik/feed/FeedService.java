package io.github.selenajiro.europolitik.feed;

import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.election.ElectionRepository;
import io.github.selenajiro.europolitik.favoritecountry.FavoriteCountryRepository;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEvent;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FeedService {

    private static final int SNIPPET_LENGTH = 180;

    private final FavoriteCountryRepository favoriteCountryRepository;
    private final PoliticalEventRepository politicalEventRepository;
    private final ElectionRepository electionRepository;
    private final int limit;
    private final int recencyMonths;

    public FeedService(FavoriteCountryRepository favoriteCountryRepository,
                       PoliticalEventRepository politicalEventRepository,
                       ElectionRepository electionRepository,
                       @Value("${europolitik.feed.limit:10}") int limit,
                       @Value("${europolitik.feed.recency-months:18}") int recencyMonths) {
        this.favoriteCountryRepository = favoriteCountryRepository;
        this.politicalEventRepository = politicalEventRepository;
        this.electionRepository = electionRepository;
        this.limit = limit;
        this.recencyMonths = recencyMonths;
    }

    public FeedResponse buildFeed(String username) {
        List<Long> favoriteCountryIds = favoriteCountryRepository.findAllByUsername(username).stream()
                .map(f -> f.getCountry().getId())
                .toList();

        boolean personalized = !favoriteCountryIds.isEmpty();
        LocalDate cutoff = LocalDate.now().minusMonths(recencyMonths);
        Pageable page = PageRequest.of(0, limit);

        List<PoliticalEvent> events = personalized
                ? politicalEventRepository.findRecentByCountryIdIn(favoriteCountryIds, cutoff, page)
                : politicalEventRepository.findRecentWithCountry(cutoff, page);

        List<Election> elections = personalized
                ? electionRepository.findRecentByCountryIdIn(favoriteCountryIds, cutoff, page)
                : electionRepository.findRecentWithCountry(cutoff, page);

        List<FeedItem> items = new ArrayList<>();

        events.forEach(e -> items.add(new FeedItem(
                "EVENT", e.getTitle(), snippet(e.getDescription()), e.getEventDate(),
                e.getCountry() != null ? e.getCountry().getId() : null,
                e.getCountry() != null ? e.getCountry().getName() : null,
                e.getCountry() != null ? e.getCountry().getIsoCode() : null,
                e.getSourceName(), e.getSourceUrl())));

        elections.forEach(e -> items.add(new FeedItem(
                "ELECTION", e.getName(), null, e.getElectionDate(),
                e.getCountry().getId(), e.getCountry().getName(), e.getCountry().getIsoCode(),
                e.getSourceName(), e.getSourceUrl())));

        List<FeedItem> sorted = items.stream()
                .sorted(Comparator.comparing(FeedItem::date).reversed())
                .limit(limit)
                .toList();

        return new FeedResponse(personalized, sorted);
    }

    private String snippet(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String trimmed = description.strip();
        return trimmed.length() > SNIPPET_LENGTH
                ? trimmed.substring(0, SNIPPET_LENGTH).strip() + "…"
                : trimmed;
    }
}
