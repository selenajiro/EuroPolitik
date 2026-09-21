package io.github.selenajiro.europolitik.election;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ElectionRepository extends JpaRepository<Election, Long> {

    @Query("SELECT e FROM Election e JOIN FETCH e.country")
    List<Election> findAllWithCountry();

    @Query("SELECT e FROM Election e JOIN FETCH e.country WHERE e.id = :id")
    Optional<Election> findByIdWithCountry(Long id);

    @Query("SELECT e FROM Election e JOIN FETCH e.country WHERE e.country.id = :countryId")
    List<Election> findAllByCountryId(Long countryId);

    @Query("SELECT e FROM Election e JOIN FETCH e.country WHERE e.country.id IN :countryIds ORDER BY e.electionDate DESC")
    List<Election> findAllByCountryIdIn(List<Long> countryIds);

    @Query("SELECT e FROM Election e JOIN FETCH e.country WHERE e.type <> 'CURRENT_COMPOSITION' AND e.electionDate >= :cutoff ORDER BY e.electionDate DESC")
    List<Election> findRecentWithCountry(@Param("cutoff") LocalDate cutoff, Pageable pageable);

    @Query("SELECT e FROM Election e JOIN FETCH e.country WHERE e.type <> 'CURRENT_COMPOSITION' AND e.country.id IN :countryIds AND e.electionDate >= :cutoff ORDER BY e.electionDate DESC")
    List<Election> findRecentByCountryIdIn(@Param("countryIds") List<Long> countryIds, @Param("cutoff") LocalDate cutoff, Pageable pageable);

    Optional<Election> findByParlgovElectionId(Long parlgovElectionId);

    Optional<Election> findByCountryIdAndType(Long countryId, String type);
}