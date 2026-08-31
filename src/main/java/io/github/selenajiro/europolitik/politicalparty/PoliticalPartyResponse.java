package io.github.selenajiro.europolitik.politicalparty;

import java.time.LocalDateTime;

public record PoliticalPartyResponse(
        Long id,
        String name,
        String shortName,
        String politicalGroup,
        String ideology,
        String sourceName,
        String sourceUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long countryId,
        String countryName
) {
    public static PoliticalPartyResponse from(PoliticalParty party) {
        return new PoliticalPartyResponse(
                party.getId(),
                party.getName(),
                party.getShortName(),
                party.getPoliticalGroup(),
                party.getIdeology(),
                party.getSourceName(),
                party.getSourceUrl(),
                party.getCreatedAt(),
                party.getUpdatedAt(),
                party.getCountry().getId(),
                party.getCountry().getName()
        );
    }
}
