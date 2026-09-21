package io.github.selenajiro.europolitik.electionresult;

import java.math.BigDecimal;

public record PartySeatsResponse(
        Long partyId,
        String partyName,
        String partyShortName,
        Integer seats,
        BigDecimal leftRight
) {
    public static PartySeatsResponse from(ElectionResult result) {
        return new PartySeatsResponse(
                result.getParty().getId(),
                result.getParty().getName(),
                result.getParty().getShortName(),
                result.getSeats(),
                result.getParty().getLeftRight()
        );
    }
}
