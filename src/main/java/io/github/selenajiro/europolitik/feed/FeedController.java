package io.github.selenajiro.europolitik.feed;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/api/feed")
    public FeedResponse feed(Authentication authentication) {
        return feedService.buildFeed(authentication.getName());
    }
}