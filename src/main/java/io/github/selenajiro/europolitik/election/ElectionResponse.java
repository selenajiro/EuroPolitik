package io.github.selenajiro.europolitik.election;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ElectionResponse(
        Long id,
        String name,
        String type,
        LocalDate electionDate,
        String sourceName,
        String sourceUrl,
        LocalDateTime importedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long countryId,
        String countryName
) {
    public static ElectionResponse from(Election election) {
        return new ElectionResponse(
                election.getId(),
                election.getName(),
                election.getType(),
                election.getElectionDate(),
                election.getSourceName(),
                election.getSourceUrl(),
                election.getImportedAt(),
                election.getCreatedAt(),
                election.getUpdatedAt(),
                election.getCountry().getId(),
                election.getCountry().getName()
        );
    }
}
