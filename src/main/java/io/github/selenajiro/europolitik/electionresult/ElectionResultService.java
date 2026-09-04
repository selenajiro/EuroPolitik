package io.github.selenajiro.europolitik.electionresult;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ElectionResultService {

    private final ElectionResultRepository electionResultRepository;

    public ElectionResultService(ElectionResultRepository electionResultRepository) {
        this.electionResultRepository = electionResultRepository;
    }

    public List<ElectionResult> findAllWithDetails() {
        return electionResultRepository.findAllWithDetails();
    }

    public Optional<ElectionResult> findByIdWithDetails(Long id) {
        return electionResultRepository.findByIdWithDetails(id);
    }
}
