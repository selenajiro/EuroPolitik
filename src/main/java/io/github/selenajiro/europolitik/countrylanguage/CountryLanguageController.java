package io.github.selenajiro.europolitik.countrylanguage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/country-languages")
public class CountryLanguageController {

    private final CountryLanguageRepository countryLanguageRepository;

    public CountryLanguageController(CountryLanguageRepository countryLanguageRepository) {
        this.countryLanguageRepository = countryLanguageRepository;
    }

    @GetMapping
    public List<CountryLanguageResponse> findAll() {
        return countryLanguageRepository.findAllWithCountry().stream()
                .map(CountryLanguageResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CountryLanguageResponse findById(@PathVariable Long id) {
        return countryLanguageRepository.findByIdWithCountry(id)
                .map(CountryLanguageResponse::from)
                .orElseThrow();
    }
}
