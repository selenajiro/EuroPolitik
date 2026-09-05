package io.github.selenajiro.europolitik.politicalparty;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PoliticalPartyService {

    private final PoliticalPartyRepository politicalPartyRepository;

    public PoliticalPartyService(PoliticalPartyRepository politicalPartyRepository) {
        this.politicalPartyRepository = politicalPartyRepository;
    }

    public List<PoliticalParty> findAllWithCountry() {
        return politicalPartyRepository.findAllWithCountry();
    }

    public Optional<PoliticalParty> findByIdWithCountry(Long id) {
        return politicalPartyRepository.findByIdWithCountry(id);
    }

    public List<PoliticalParty> findAllByCountryId(Long countryId) {
        return politicalPartyRepository.findAllByCountryId(countryId);
    }
}
