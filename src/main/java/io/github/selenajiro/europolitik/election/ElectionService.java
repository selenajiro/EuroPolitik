package io.github.selenajiro.europolitik.election;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ElectionService {

    private final ElectionRepository electionRepository;

    public ElectionService(ElectionRepository electionRepository) {
        this.electionRepository = electionRepository;
    }

    public List<Election> findAllWithCountry() {
        return electionRepository.findAllWithCountry();
    }

    public Optional<Election> findByIdWithCountry(Long id) {
        return electionRepository.findByIdWithCountry(id);
    }
}
