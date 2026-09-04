package io.github.selenajiro.europolitik.politicalevent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/political-events")
public class PoliticalEventController {

    private final PoliticalEventService politicalEventService;

    public PoliticalEventController(PoliticalEventService politicalEventService) {
        this.politicalEventService = politicalEventService;
    }

    @GetMapping
    public List<PoliticalEventResponse> findAll() {
        return politicalEventService.findAllWithCountry().stream()
                .map(PoliticalEventResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PoliticalEventResponse findById(@PathVariable Long id) {
        return politicalEventService.findByIdWithCountry(id)
                .map(PoliticalEventResponse::from)
                .orElseThrow();
    }
}
