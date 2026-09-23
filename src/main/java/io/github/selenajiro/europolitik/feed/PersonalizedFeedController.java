package io.github.selenajiro.europolitik.feed;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PersonalizedFeedController {

    private final PersonalizedFeedService personalizedFeedService;

    public PersonalizedFeedController(PersonalizedFeedService personalizedFeedService) {
        this.personalizedFeedService = personalizedFeedService;
    }

    @GetMapping("/api/feed/full")
    public PersonalizedFeedResponse feed(Authentication authentication,
                                         @RequestParam(defaultValue = "0") int offset,
                                         @RequestParam(defaultValue = "10") int limit) {
        return personalizedFeedService.buildPage(authentication.getName(), offset, Math.min(limit, 50));
    }
}