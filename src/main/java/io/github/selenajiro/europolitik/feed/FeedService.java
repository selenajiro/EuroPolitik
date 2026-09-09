package io.github.selenajiro.europolitik.feed;

import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.election.ElectionRepository;
import io.github.selenajiro.europolitik.favoritecountry.FavoriteCountryRepository;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEvent;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FeedService {

    private static final int LIMIT = 10;

    private final FavoriteCountryRepository favoriteCountryRepository;
    private final PoliticalEventRepository politicalEventRepository;
    private final ElectionRepository electionRepository;

    public FeedService(FavoriteCountryRepository favoriteCountryRepository,
                       PoliticalEventRepository politicalEventRepository,
                       ElectionRepository electionRepository) {
        this.favoriteCountryRepository = favoriteCountryRepository;
        this.politicalEventRepository = politicalEventRepository;
        this.electionRepository = electionRepository;
    }

    public FeedResponse buildFeed(String username) {
        List<Long> favoriteCountryIds = favoriteCountryRepository.findAllByUsername(username).stream()
                .map(f -> f.getCountry().getId())
                .toList();

        boolean personalized = !favoriteCountryIds.isEmpty();

        List<PoliticalEvent> events = personalized
                ? politicalEventRepository.findAllByCountryIdIn(favoriteCountryIds)
                : politicalEventRepository.findAllWithCountry();

        List<Election> elections = personalized
                ? electionRepository.findAllByCountryIdIn(favoriteCountryIds)
                : electionRepository.findAllWithCountry();

        List<FeedItem> items = new ArrayList<>();

        events.forEach(e -> items.add(new FeedItem(
                "EVENT", e.getTitle(), e.getEventDate(),
                e.getCountry() != null ? e.getCountry().getId() : null,
                e.getCountry() != null ? e.getCountry().getName() : null)));

        elections.forEach(e -> items.add(new FeedItem(
                "ELECTION", e.getName(), e.getElectionDate(),
                e.getCountry().getId(), e.getCountry().getName())));

        List<FeedItem> sorted = items.stream()
                .sorted(Comparator.comparing(FeedItem::date).reversed())
                .limit(LIMIT)
                .toList();

        return new FeedResponse(personalized, sorted);
    }
}
