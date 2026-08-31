package io.github.selenajiro.europolitik.country;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryRepository countryRepository;

    public CountryController(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @GetMapping
    public List<Country> findAll() {
        return countryRepository.findAll();
    }

    @GetMapping("/{id}")
    public Country findById(@PathVariable Long id) {
        return countryRepository.findById(id).orElseThrow();
    }
}
