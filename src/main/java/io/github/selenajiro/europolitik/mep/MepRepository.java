package io.github.selenajiro.europolitik.mep;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MepRepository extends JpaRepository<Mep, Long> {

    @Query("SELECT m FROM Mep m JOIN FETCH m.country")
    List<Mep> findAllWithCountry();

    @Query("SELECT m FROM Mep m JOIN FETCH m.country WHERE m.id = :id")
    Optional<Mep> findByIdWithCountry(Long id);

    @Query("SELECT m FROM Mep m JOIN FETCH m.country WHERE m.country.id = :countryId")
    List<Mep> findAllByCountryId(Long countryId);
}
