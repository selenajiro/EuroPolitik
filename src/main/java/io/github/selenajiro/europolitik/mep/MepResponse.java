package io.github.selenajiro.europolitik.mep;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MepResponse(
        Long id,
        String fullName,
        String nationalParty,
        String politicalGroup,
        String parliamentaryTerm,
        LocalDate startDate,
        LocalDate endDate,
        String sourceName,
        String sourceUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long countryId,
        String countryName
) {
    public static MepResponse from(Mep mep) {
        return new MepResponse(
                mep.getId(),
                mep.getFullName(),
                mep.getNationalParty(),
                mep.getPoliticalGroup(),
                mep.getParliamentaryTerm(),
                mep.getStartDate(),
                mep.getEndDate(),
                mep.getSourceName(),
                mep.getSourceUrl(),
                mep.getCreatedAt(),
                mep.getUpdatedAt(),
                mep.getCountry().getId(),
                mep.getCountry().getName()
        );
    }
}
