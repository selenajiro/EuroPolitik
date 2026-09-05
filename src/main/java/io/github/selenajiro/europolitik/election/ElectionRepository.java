package io.github.selenajiro.europolitik.election;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ElectionRepository extends JpaRepository<Election, Long> {

    @Query("SELECT e FROM Election e JOIN FETCH e.country")
    List<Election> findAllWithCountry();

    @Query("SELECT e FROM Election e JOIN FETCH e.country WHERE e.id = :id")
    Optional<Election> findByIdWithCountry(Long id);

    @Query("SELECT e FROM Election e JOIN FETCH e.country WHERE e.country.id = :countryId")
    List<Election> findAllByCountryId(Long countryId);
}