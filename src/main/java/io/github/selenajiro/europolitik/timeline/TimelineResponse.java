package io.github.selenajiro.europolitik.timeline;

import io.github.selenajiro.europolitik.feed.FeedItem;

import java.util.List;

public record TimelineResponse(List<FeedItem> items, boolean hasMore) {}
