package io.github.selenajiro.europolitik.countrystatistic;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CountryStatisticService {

    private final CountryStatisticRepository countryStatisticRepository;

    public CountryStatisticService(CountryStatisticRepository countryStatisticRepository) {
        this.countryStatisticRepository = countryStatisticRepository;
    }

    public List<CountryStatistic> findAllWithCountry() {
        return countryStatisticRepository.findAllWithCountry();
    }

    public Optional<CountryStatistic> findByIdWithCountry(Long id) {
        return countryStatisticRepository.findByIdWithCountry(id);
    }
}
