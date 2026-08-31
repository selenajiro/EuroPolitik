package io.github.selenajiro.europolitik.countrystatistic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CountryStatisticRepository extends JpaRepository<CountryStatistic, Long> {

    @Query("SELECT s FROM CountryStatistic s JOIN FETCH s.country")
    List<CountryStatistic> findAllWithCountry();

    @Query("SELECT s FROM CountryStatistic s JOIN FETCH s.country WHERE s.id = :id")
    Optional<CountryStatistic> findByIdWithCountry(Long id);
}