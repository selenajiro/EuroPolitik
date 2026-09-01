package io.github.selenajiro.europolitik.countrylanguage;

import java.time.LocalDateTime;

public record CountryLanguageResponse(
        Long id,
        String language,
        boolean official,
        boolean minority,
        String languageFamily,
        String sourceName,
        String sourceUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long countryId,
        String countryName
) {
    public static CountryLanguageResponse from(CountryLanguage lang) {
        return new CountryLanguageResponse(
                lang.getId(),
                lang.getLanguage(),
                lang.isOfficial(),
                lang.isMinority(),
                lang.getLanguageFamily(),
                lang.getSourceName(),
                lang.getSourceUrl(),
                lang.getCreatedAt(),
                lang.getUpdatedAt(),
                lang.getCountry().getId(),
                lang.getCountry().getName()
        );
    }
}
