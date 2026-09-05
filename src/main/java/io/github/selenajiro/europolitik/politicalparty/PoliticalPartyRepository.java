package io.github.selenajiro.europolitik.politicalparty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PoliticalPartyRepository extends JpaRepository<PoliticalParty, Long> {

    @Query("SELECT p FROM PoliticalParty p JOIN FETCH p.country")
    List<PoliticalParty> findAllWithCountry();

    @Query("SELECT p FROM PoliticalParty p JOIN FETCH p.country WHERE p.id = :id")
    Optional<PoliticalParty> findByIdWithCountry(Long id);

    @Query("SELECT p FROM PoliticalParty p JOIN FETCH p.country WHERE p.country.id = :countryId")
    List<PoliticalParty> findAllByCountryId(Long countryId);
}
