package io.github.selenajiro.europolitik.electionresult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/election-results")
public class ElectionResultController {

    private final ElectionResultService electionResultService;

    public ElectionResultController(ElectionResultService electionResultService) {
        this.electionResultService = electionResultService;
    }

    @GetMapping
    public List<ElectionResultResponse> findAll() {
        return electionResultService.findAllWithDetails().stream()
                .map(ElectionResultResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ElectionResultResponse findById(@PathVariable Long id) {
        return electionResultService.findByIdWithDetails(id)
                .map(ElectionResultResponse::from)
                .orElseThrow();
    }
}
