package io.github.selenajiro.europolitik.countrylanguage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CountryLanguageRepository extends JpaRepository<CountryLanguage, Long> {

    @Query("SELECT l FROM CountryLanguage l JOIN FETCH l.country")
    List<CountryLanguage> findAllWithCountry();

    @Query("SELECT l FROM CountryLanguage l JOIN FETCH l.country WHERE l.id = :id")
    Optional<CountryLanguage> findByIdWithCountry(Long id);
}
