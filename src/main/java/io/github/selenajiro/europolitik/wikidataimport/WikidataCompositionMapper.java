package io.github.selenajiro.europolitik.wikidataimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.electionresult.ElectionResult;
import io.github.selenajiro.europolitik.politicalparty.PoliticalParty;
import io.github.selenajiro.europolitik.wikidataimport.SparqlResultsDto.SparqlValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class WikidataCompositionMapper {

    private static final String SOURCE_NAME = "Wikidata";
    private static final String SOURCE_URL = "https://www.wikidata.org";
    private static final String ELECTION_TYPE = "CURRENT_COMPOSITION";

    private WikidataCompositionMapper() {
    }

    public record NormalizedPartyComposition(String wikidataPartyId, String partyLabel, int seats) {}

    public static NormalizedPartyComposition normalize(Map<String, SparqlValue> binding) {
        if (binding == null) {
            return null;
        }

        SparqlValue partyValue = binding.get("party");
        SparqlValue labelValue = binding.get("partyLabel");
        SparqlValue countValue = binding.get("count");

        if (partyValue == null || isBlank(partyValue.value())
                || labelValue == null || isBlank(labelValue.value())
                || countValue == null || isBlank(countValue.value())) {
            return null;
        }

        String uri = partyValue.value();
        String qid = uri.substring(uri.lastIndexOf('/') + 1);
        if (isBlank(qid)) {
            return null;
        }

        int seats;
        try {
            seats = Integer.parseInt(countValue.value().trim());
        } catch (NumberFormatException e) {
            return null;
        }
        if (seats <= 0) {
            return null;
        }

        return new NormalizedPartyComposition(qid, labelValue.value().trim(), seats);
    }

    public static boolean applyToParty(PoliticalParty party, NormalizedPartyComposition normalized, Country country) {
        boolean changed = false;
        if (!normalized.wikidataPartyId().equals(party.getWikidataPartyId())) {
            party.setWikidataPartyId(normalized.wikidataPartyId());
            changed = true;
        }
        if (party.getCountry() == null || !Objects.equals(party.getCountry().getId(), country.getId())) {
            party.setCountry(country);
            changed = true;
        }
        if (!normalized.partyLabel().equals(party.getName())) {
            party.setName(normalized.partyLabel());
            changed = true;
        }
        if (party.getSourceName() == null) {
            party.setSourceName(SOURCE_NAME);
            party.setSourceUrl(SOURCE_URL);
            changed = true;
        }

        LocalDateTime now = LocalDateTime.now();
        if (party.getCreatedAt() == null) {
            party.setCreatedAt(now);
        }
        if (changed) {
            party.setUpdatedAt(now);
        }
        return changed;
    }

    public static boolean applyToElection(Election election, Country country) {
        boolean changed = false;
        LocalDate today = LocalDate.now();

        if (election.getCountry() == null || !Objects.equals(election.getCountry().getId(), country.getId())) {
            election.setCountry(country);
            changed = true;
        }
        if (!ELECTION_TYPE.equals(election.getType())) {
            election.setType(ELECTION_TYPE);
            changed = true;
        }
        if (!today.equals(election.getElectionDate())) {
            election.setElectionDate(today);
            changed = true;
        }
        String name = country.getName() + " current parliament composition";
        if (!name.equals(election.getName())) {
            election.setName(name);
            changed = true;
        }
        if (election.getSourceName() == null) {
            election.setSourceName(SOURCE_NAME);
            election.setSourceUrl(SOURCE_URL);
            changed = true;
        }

        LocalDateTime now = LocalDateTime.now();
        election.setImportedAt(now); // always refresh -- this field means "last checked"
        if (election.getCreatedAt() == null) {
            election.setCreatedAt(now);
        }
        if (changed) {
            election.setUpdatedAt(now);
        }
        return changed;
    }

    public static boolean applyToResult(ElectionResult result, NormalizedPartyComposition normalized, Election election, PoliticalParty party) {
        boolean changed = false;
        if (result.getElection() == null || !Objects.equals(result.getElection().getId(), election.getId())) {
            result.setElection(election);
            changed = true;
        }
        if (result.getParty() == null || !Objects.equals(result.getParty().getId(), party.getId())) {
            result.setParty(party);
            changed = true;
        }
        if (!Objects.equals(result.getSeats(), normalized.seats())) {
            result.setSeats(normalized.seats());
            changed = true;
        }

        LocalDateTime now = LocalDateTime.now();
        if (result.getCreatedAt() == null) {
            result.setCreatedAt(now);
        }
        if (changed) {
            result.setUpdatedAt(now);
        }
        return changed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
