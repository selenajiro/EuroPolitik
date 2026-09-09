package io.github.selenajiro.europolitik.feed;

import java.util.List;

public record FeedResponse(
        boolean personalized,
        List<FeedItem> items
) {}
