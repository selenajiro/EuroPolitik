package io.github.selenajiro.europolitik.savedcomparison;

import java.time.LocalDateTime;

public record SavedComparisonResponse(
        Long id,
        Long countryAId,
        String countryAName,
        Long countryBId,
        String countryBName,
        LocalDateTime createdAt
) {
    public static SavedComparisonResponse from(SavedComparison comparison) {
        return new SavedComparisonResponse(
                comparison.getId(),
                comparison.getCountryA().getId(),
                comparison.getCountryA().getName(),
                comparison.getCountryB().getId(),
                comparison.getCountryB().getName(),
                comparison.getCreatedAt()
        );
    }
}
