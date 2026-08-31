package io.github.selenajiro.europolitik.politicalevent;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/political-events")
public class PoliticalEventController {

    private final PoliticalEventRepository politicalEventRepository;

    public PoliticalEventController(PoliticalEventRepository politicalEventRepository) {
        this.politicalEventRepository = politicalEventRepository;
    }

    @GetMapping
    public List<PoliticalEventResponse> findAll() {
        return politicalEventRepository.findAllWithCountry().stream()
                .map(PoliticalEventResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PoliticalEventResponse findById(@PathVariable Long id) {
        return politicalEventRepository.findByIdWithCountry(id)
                .map(PoliticalEventResponse::from)
                .orElseThrow();
    }
}
