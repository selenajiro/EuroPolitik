package io.github.selenajiro.europolitik.politicalevent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PoliticalEventRepository extends JpaRepository<PoliticalEvent, Long> {

    @Query("SELECT e FROM PoliticalEvent e LEFT JOIN FETCH e.country ORDER BY e.eventDate DESC")
    List<PoliticalEvent> findAllWithCountry();

    @Query("SELECT e FROM PoliticalEvent e LEFT JOIN FETCH e.country WHERE e.id = :id")
    Optional<PoliticalEvent> findByIdWithCountry(Long id);

    @Query("SELECT e FROM PoliticalEvent e LEFT JOIN FETCH e.country WHERE e.country.id = :countryId ORDER BY e.eventDate DESC")
    List<PoliticalEvent> findAllByCountryId(Long countryId);

    @Query("SELECT e FROM PoliticalEvent e LEFT JOIN FETCH e.country WHERE e.country.id IN :countryIds ORDER BY e.eventDate DESC")
    List<PoliticalEvent> findAllByCountryIdIn(List<Long> countryIds);
}
