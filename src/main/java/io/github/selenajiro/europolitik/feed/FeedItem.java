package io.github.selenajiro.europolitik.feed;

import java.time.LocalDate;

public record FeedItem(
        String type,
        String title,
        String description,
        LocalDate date,
        Long countryId,
        String countryName,
        String countryIsoCode,
        String sourceName,
        String sourceUrl
) {}