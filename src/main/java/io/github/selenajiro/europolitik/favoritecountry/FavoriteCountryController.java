package io.github.selenajiro.europolitik.favoritecountry;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteCountryController {

    private final FavoriteCountryService favoriteCountryService;

    public FavoriteCountryController(FavoriteCountryService favoriteCountryService) {
        this.favoriteCountryService = favoriteCountryService;
    }

    @GetMapping
    public List<FavoriteCountryResponse> findAll(Authentication authentication) {
        return favoriteCountryService.findAllForUser(authentication.getName()).stream()
                .map(FavoriteCountryResponse::from)
                .toList();
    }

    @PostMapping("/{countryId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteCountryResponse add(@PathVariable Long countryId, Authentication authentication) {
        return FavoriteCountryResponse.from(favoriteCountryService.addFavorite(authentication.getName(), countryId));
    }

    @DeleteMapping("/{countryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long countryId, Authentication authentication) {
        favoriteCountryService.removeFavorite(authentication.getName(), countryId);
    }
}
