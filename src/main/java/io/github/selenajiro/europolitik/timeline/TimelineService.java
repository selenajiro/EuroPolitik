package io.github.selenajiro.europolitik.timeline;

import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.election.ElectionRepository;
import io.github.selenajiro.europolitik.feed.FeedItem;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEvent;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class TimelineService {

    private static final String COMPOSITION_TYPE = "CURRENT_COMPOSITION";

    private final PoliticalEventRepository politicalEventRepository;
    private final ElectionRepository electionRepository;

    public TimelineService(PoliticalEventRepository politicalEventRepository, ElectionRepository electionRepository) {
        this.politicalEventRepository = politicalEventRepository;
        this.electionRepository = electionRepository;
    }

    public TimelineResponse buildPage(int offset, int limit, String search, String type, Long countryId) {
        List<ScoredItem> all = new ArrayList<>();

        List<PoliticalEvent> events = countryId != null
                ? politicalEventRepository.findAllByCountryId(countryId)
                : politicalEventRepository.findAllWithCountry();
        for (PoliticalEvent e : events) {
            if (e.getEventDate() == null) {
                continue;
            }
            all.add(new ScoredItem(e.getEventDate(), e.getEventType(), toFeedItem(e)));
        }

        List<Election> elections = countryId != null
                ? electionRepository.findAllByCountryId(countryId)
                : electionRepository.findAllWithCountry();
        for (Election e : elections) {
            if (COMPOSITION_TYPE.equals(e.getType()) || e.getElectionDate() == null) {
                continue;
            }
            all.add(new ScoredItem(e.getElectionDate(), "ELECTION", toFeedItem(e)));
        }

        String searchLower = (search == null) ? null : search.toLowerCase(Locale.ENGLISH).trim();

        List<ScoredItem> filtered = all.stream()
                .filter(item -> type == null || type.isBlank() || type.equalsIgnoreCase(item.rawType()))
                .filter(item -> searchLower == null || searchLower.isEmpty()
                        || item.item().title().toLowerCase(Locale.ENGLISH).contains(searchLower))
                .sorted(Comparator.comparing(ScoredItem::date).reversed())
                .toList();

        int from = Math.min(offset, filtered.size());
        int to = Math.min(offset + limit, filtered.size());
        boolean hasMore = to < filtered.size();

        return new TimelineResponse(filtered.subList(from, to).stream().map(ScoredItem::item).toList(), hasMore);
    }

    private record ScoredItem(LocalDate date, String rawType, FeedItem item) {}

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

    private String snippet(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        String trimmed = description.strip();
        return trimmed.length() > 180 ? trimmed.substring(0, 180).strip() + "…" : trimmed;
    }
}
