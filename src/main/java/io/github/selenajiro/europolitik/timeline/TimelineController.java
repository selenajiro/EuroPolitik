package io.github.selenajiro.europolitik.timeline;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class TimelineController {

    private final TimelineService timelineService;

    public TimelineController(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping("/api/timeline")
    public TimelineResponse timeline(@RequestParam(defaultValue = "0") int offset,
                                     @RequestParam(defaultValue = "20") int limit,
                                     @RequestParam(required = false) String search,
                                     @RequestParam(required = false) String type,
                                     @RequestParam(required = false) Long countryId,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return timelineService.buildPage(offset, Math.min(limit, 100), search, type, countryId, dateFrom, dateTo);
    }
}
