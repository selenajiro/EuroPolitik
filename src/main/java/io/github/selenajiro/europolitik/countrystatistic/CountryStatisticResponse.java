package io.github.selenajiro.europolitik.countrystatistic;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CountryStatisticResponse(
        Long id,
        String indicator,
        Integer year,
        BigDecimal value,
        String unit,
        String sourceName,
        String sourceUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long countryId,
        String countryName
) {
    public static CountryStatisticResponse from(CountryStatistic stat) {
        return new CountryStatisticResponse(
                stat.getId(),
                stat.getIndicator(),
                stat.getYear(),
                stat.getValue(),
                stat.getUnit(),
                stat.getSourceName(),
                stat.getSourceUrl(),
                stat.getCreatedAt(),
                stat.getUpdatedAt(),
                stat.getCountry().getId(),
                stat.getCountry().getName()
        );
    }
}