package io.github.selenajiro.europolitik.savedcomparison;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.user.UserAccount;
import io.github.selenajiro.europolitik.user.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SavedComparisonService {

    private final SavedComparisonRepository savedComparisonRepository;
    private final CountryRepository countryRepository;
    private final UserAccountRepository userAccountRepository;

    public SavedComparisonService(SavedComparisonRepository savedComparisonRepository,
                                  CountryRepository countryRepository,
                                  UserAccountRepository userAccountRepository) {
        this.savedComparisonRepository = savedComparisonRepository;
        this.countryRepository = countryRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public List<SavedComparison> findAllForUser(String username) {
        return savedComparisonRepository.findAllByUsername(username);
    }

    @Transactional
    public SavedComparison save(String username, Long countryAId, Long countryBId) {
        if (countryAId.equals(countryBId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot compare a country with itself");
        }

        UserAccount user = userAccountRepository.findByUsername(username).orElseThrow();
        Country countryA = countryRepository.findById(countryAId).orElseThrow();
        Country countryB = countryRepository.findById(countryBId).orElseThrow();

        SavedComparison comparison = new SavedComparison();
        comparison.setUser(user);
        comparison.setCountryA(countryA);
        comparison.setCountryB(countryB);
        comparison.setCreatedAt(LocalDateTime.now());
        return savedComparisonRepository.save(comparison);
    }

    @Transactional
    public void delete(String username, Long id) {
        SavedComparison comparison = savedComparisonRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comparison not found"));
        savedComparisonRepository.delete(comparison);
    }
}
