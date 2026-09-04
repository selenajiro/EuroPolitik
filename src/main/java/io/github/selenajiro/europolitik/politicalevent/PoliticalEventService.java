package io.github.selenajiro.europolitik.politicalevent;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PoliticalEventService {

    private final PoliticalEventRepository politicalEventRepository;

    public PoliticalEventService(PoliticalEventRepository politicalEventRepository) {
        this.politicalEventRepository = politicalEventRepository;
    }

    public List<PoliticalEvent> findAllWithCountry() {
        return politicalEventRepository.findAllWithCountry();
    }

    public Optional<PoliticalEvent> findByIdWithCountry(Long id) {
        return politicalEventRepository.findByIdWithCountry(id);
    }
}
