package io.github.selenajiro.europolitik.election;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/elections")
public class ElectionController {

    private final ElectionService electionService;

    public ElectionController(ElectionService electionService) {
        this.electionService = electionService;
    }

    @GetMapping
    public List<ElectionResponse> findAll() {
        return electionService.findAllWithCountry().stream()
                .map(ElectionResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ElectionResponse findById(@PathVariable Long id) {
        return electionService.findByIdWithCountry(id)
                .map(ElectionResponse::from)
                .orElseThrow();
    }
}