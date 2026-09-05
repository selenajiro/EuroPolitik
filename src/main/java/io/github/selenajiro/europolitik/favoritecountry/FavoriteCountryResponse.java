package io.github.selenajiro.europolitik.favoritecountry;

import java.time.LocalDateTime;

public record FavoriteCountryResponse(
        Long id,
        Long countryId,
        String countryName,
        String countryIsoCode,
        LocalDateTime createdAt
) {
    public static FavoriteCountryResponse from(FavoriteCountry favorite) {
        return new FavoriteCountryResponse(
                favorite.getId(),
                favorite.getCountry().getId(),
                favorite.getCountry().getName(),
                favorite.getCountry().getIsoCode(),
                favorite.getCreatedAt()
        );
    }
}