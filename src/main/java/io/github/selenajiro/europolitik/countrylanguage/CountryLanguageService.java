package io.github.selenajiro.europolitik.countrylanguage;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CountryLanguageService {

    private final CountryLanguageRepository countryLanguageRepository;

    public CountryLanguageService(CountryLanguageRepository countryLanguageRepository) {
        this.countryLanguageRepository = countryLanguageRepository;
    }

    public List<CountryLanguage> findAllWithCountry() {
        return countryLanguageRepository.findAllWithCountry();
    }

    public Optional<CountryLanguage> findByIdWithCountry(Long id) {
        return countryLanguageRepository.findByIdWithCountry(id);
    }
}