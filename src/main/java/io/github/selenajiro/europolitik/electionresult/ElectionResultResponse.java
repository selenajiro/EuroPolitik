package io.github.selenajiro.europolitik.electionresult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ElectionResultResponse(
        Long id,
        Long votes,
        BigDecimal votePercentage,
        Integer seats,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long electionId,
        String electionName,
        Long partyId,
        String partyName
) {
    public static ElectionResultResponse from(ElectionResult result) {
        return new ElectionResultResponse(
                result.getId(),
                result.getVotes(),
                result.getVotePercentage(),
                result.getSeats(),
                result.getCreatedAt(),
                result.getUpdatedAt(),
                result.getElection().getId(),
                result.getElection().getName(),
                result.getParty().getId(),
                result.getParty().getName()
        );
    }
}
