package io.github.selenajiro.europolitik.country;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CountryResponse(
        Long id,
        String isoCode,
        String name,
        boolean euMember,
        boolean schengenMember,
        boolean eurozoneMember,
        boolean natoMember,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String currentRulingParty,
        BigDecimal currentPoliticalLeaning,
        String currentPoliticalLeaningLabel,
        BigDecimal currentParliamentLeaning,
        String currentParliamentLeaningLabel
) {
    public static CountryResponse from(Country country) {
        return new CountryResponse(
                country.getId(),
                country.getIsoCode(),
                country.getName(),
                country.isEuMember(),
                country.isSchengenMember(),
                country.isEurozoneMember(),
                country.isNatoMember(),
                country.getCreatedAt(),
                country.getUpdatedAt(),
                country.getCurrentRulingParty(),
                country.getCurrentPoliticalLeaning(),
                country.getCurrentPoliticalLeaningLabel(),
                country.getCurrentParliamentLeaning(),
                country.getCurrentParliamentLeaningLabel()
        );
    }
}