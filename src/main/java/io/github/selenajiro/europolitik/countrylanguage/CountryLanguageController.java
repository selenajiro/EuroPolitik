package io.github.selenajiro.europolitik.countrylanguage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/country-languages")
public class CountryLanguageController {

    private final CountryLanguageService countryLanguageService;

    public CountryLanguageController(CountryLanguageService countryLanguageService) {
        this.countryLanguageService = countryLanguageService;
    }

    @GetMapping
    public List<CountryLanguageResponse> findAll() {
        return countryLanguageService.findAllWithCountry().stream()
                .map(CountryLanguageResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CountryLanguageResponse findById(@PathVariable Long id) {
        return countryLanguageService.findByIdWithCountry(id)
                .map(CountryLanguageResponse::from)
                .orElseThrow();
    }
}
