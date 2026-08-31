package io.github.selenajiro.europolitik.politicalevent;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PoliticalEventResponse(
        Long id,
        String eventType,
        String title,
        String description,
        LocalDate eventDate,
        String sourceName,
        String sourceUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long countryId,
        String countryName
) {
    public static PoliticalEventResponse from(PoliticalEvent event) {
        return new PoliticalEventResponse(
                event.getId(),
                event.getEventType(),
                event.getTitle(),
                event.getDescription(),
                event.getEventDate(),
                event.getSourceName(),
                event.getSourceUrl(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getCountry() != null ? event.getCountry().getId() : null,
                event.getCountry() != null ? event.getCountry().getName() : null
        );
    }
}
