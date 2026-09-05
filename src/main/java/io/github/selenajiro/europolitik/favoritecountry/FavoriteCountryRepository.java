package io.github.selenajiro.europolitik.favoritecountry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FavoriteCountryRepository extends JpaRepository<FavoriteCountry, Long> {

    @Query("SELECT f FROM FavoriteCountry f JOIN FETCH f.country WHERE f.user.username = :username")
    List<FavoriteCountry> findAllByUsername(String username);

    Optional<FavoriteCountry> findByUserUsernameAndCountryId(String username, Long countryId);
}
