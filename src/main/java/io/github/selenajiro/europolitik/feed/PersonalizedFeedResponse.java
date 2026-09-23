package io.github.selenajiro.europolitik.feed;

import java.util.List;

public record PersonalizedFeedResponse(List<FeedItem> items, boolean hasMore) {}
