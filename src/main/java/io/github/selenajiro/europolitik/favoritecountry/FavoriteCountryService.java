package io.github.selenajiro.europolitik.favoritecountry;

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
public class FavoriteCountryService {

    private final FavoriteCountryRepository favoriteCountryRepository;
    private final CountryRepository countryRepository;
    private final UserAccountRepository userAccountRepository;

    public FavoriteCountryService(FavoriteCountryRepository favoriteCountryRepository,
                                  CountryRepository countryRepository,
                                  UserAccountRepository userAccountRepository) {
        this.favoriteCountryRepository = favoriteCountryRepository;
        this.countryRepository = countryRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public List<FavoriteCountry> findAllForUser(String username) {
        return favoriteCountryRepository.findAllByUsername(username);
    }

    @Transactional
    public FavoriteCountry addFavorite(String username, Long countryId) {
        if (favoriteCountryRepository.findByUserUsernameAndCountryId(username, countryId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already favorited");
        }

        UserAccount user = userAccountRepository.findByUsername(username).orElseThrow();
        Country country = countryRepository.findById(countryId).orElseThrow();

        FavoriteCountry favorite = new FavoriteCountry();
        favorite.setUser(user);
        favorite.setCountry(country);
        favorite.setCreatedAt(LocalDateTime.now());
        return favoriteCountryRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(String username, Long countryId) {
        FavoriteCountry favorite = favoriteCountryRepository.findByUserUsernameAndCountryId(username, countryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found"));
        favoriteCountryRepository.delete(favorite);
    }
}
