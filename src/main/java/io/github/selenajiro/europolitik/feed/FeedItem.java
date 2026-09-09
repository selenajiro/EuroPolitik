package io.github.selenajiro.europolitik.feed;

import java.time.LocalDate;

public record FeedItem(
        String type,
        String title,
        LocalDate date,
        Long countryId,
        String countryName
) {}
