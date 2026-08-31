package io.github.selenajiro.europolitik.countrystatistic;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/country-statistics")
public class CountryStatisticController {

    private final CountryStatisticRepository countryStatisticRepository;

    public CountryStatisticController(CountryStatisticRepository countryStatisticRepository) {
        this.countryStatisticRepository = countryStatisticRepository;
    }

    @GetMapping
    public List<CountryStatisticResponse> findAll() {
        return countryStatisticRepository.findAllWithCountry().stream()
                .map(CountryStatisticResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CountryStatisticResponse findById(@PathVariable Long id) {
        return countryStatisticRepository.findByIdWithCountry(id)
                .map(CountryStatisticResponse::from)
                .orElseThrow();
    }
}