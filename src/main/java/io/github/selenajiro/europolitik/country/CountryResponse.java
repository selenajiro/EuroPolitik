package io.github.selenajiro.europolitik.country;

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
        LocalDateTime updatedAt
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
                country.getUpdatedAt()
        );
    }
}
