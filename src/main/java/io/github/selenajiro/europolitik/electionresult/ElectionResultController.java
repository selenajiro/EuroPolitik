package io.github.selenajiro.europolitik.electionresult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/election-results")
public class ElectionResultController {

    private final ElectionResultRepository electionResultRepository;

    public ElectionResultController(ElectionResultRepository electionResultRepository) {
        this.electionResultRepository = electionResultRepository;
    }

    @GetMapping
    public List<ElectionResultResponse> findAll() {
        return electionResultRepository.findAllWithDetails().stream()
                .map(ElectionResultResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ElectionResultResponse findById(@PathVariable Long id) {
        return electionResultRepository.findByIdWithDetails(id)
                .map(ElectionResultResponse::from)
                .orElseThrow();
    }
}
