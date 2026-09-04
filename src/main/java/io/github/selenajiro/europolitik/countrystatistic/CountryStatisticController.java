package io.github.selenajiro.europolitik.countrystatistic;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/country-statistics")
public class CountryStatisticController {

    private final CountryStatisticService countryStatisticService;

    public CountryStatisticController(CountryStatisticService countryStatisticService) {
        this.countryStatisticService = countryStatisticService;
    }

    @GetMapping
    public List<CountryStatisticResponse> findAll() {
        return countryStatisticService.findAllWithCountry().stream()
                .map(CountryStatisticResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CountryStatisticResponse findById(@PathVariable Long id) {
        return countryStatisticService.findByIdWithCountry(id)
                .map(CountryStatisticResponse::from)
                .orElseThrow();
    }
}