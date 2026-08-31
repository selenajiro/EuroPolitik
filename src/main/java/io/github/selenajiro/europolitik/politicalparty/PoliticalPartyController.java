package io.github.selenajiro.europolitik.politicalparty;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/political-parties")
public class PoliticalPartyController {

    private final PoliticalPartyRepository politicalPartyRepository;

    public PoliticalPartyController(PoliticalPartyRepository politicalPartyRepository) {
        this.politicalPartyRepository = politicalPartyRepository;
    }

    @GetMapping
    public List<PoliticalPartyResponse> findAll() {
        return politicalPartyRepository.findAllWithCountry().stream()
                .map(PoliticalPartyResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PoliticalPartyResponse findById(@PathVariable Long id) {
        return politicalPartyRepository.findByIdWithCountry(id)
                .map(PoliticalPartyResponse::from)
                .orElseThrow();
    }
}