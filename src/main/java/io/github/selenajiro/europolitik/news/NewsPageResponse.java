package io.github.selenajiro.europolitik.news;

import io.github.selenajiro.europolitik.feed.FeedItem;

import java.util.List;

public record NewsPageResponse(List<FeedItem> items, boolean hasMore) {}
