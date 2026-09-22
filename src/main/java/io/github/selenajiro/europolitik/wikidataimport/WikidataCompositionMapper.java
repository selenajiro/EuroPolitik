package io.github.selenajiro.europolitik.wikidataimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.electionresult.ElectionResult;
import io.github.selenajiro.europolitik.politicalparty.PoliticalParty;
import io.github.selenajiro.europolitik.wikidataimport.SparqlResultsDto.SparqlValue;

import java.text.Normalizer;
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

    /**
     * countryIsoCode is used only to build a stable, per-country sentinel id
     * for members with no wdt:P102 (member of political party) statement on
     * Wikidata -- common for less-prominent members. They're surfaced as an
     * explicit "Not recorded on Wikidata" bucket (renders as the same no-data
     * gray used elsewhere) rather than silently dropped, which previously
     * undercounted legislatures massively.
     */
    public static NormalizedPartyComposition normalize(Map<String, SparqlValue> binding, String countryIsoCode) {
        if (binding == null) {
            return null;
        }

        SparqlValue countValue = binding.get("count");
        if (countValue == null || isBlank(countValue.value())) {
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

        SparqlValue partyValue = binding.get("party");
        SparqlValue labelValue = binding.get("partyLabel");

        if (partyValue == null || isBlank(partyValue.value()) || labelValue == null || isBlank(labelValue.value())) {
            return new NormalizedPartyComposition("UNKNOWN-" + countryIsoCode, "Not recorded on Wikidata", seats);
        }

        String uri = partyValue.value();
        String qid = uri.substring(uri.lastIndexOf('/') + 1);
        if (isBlank(qid)) {
            return new NormalizedPartyComposition("UNKNOWN-" + countryIsoCode, "Not recorded on Wikidata", seats);
        }

        return new NormalizedPartyComposition(qid, labelValue.value().trim(), seats);
    }

    public static boolean namesMatch(String wikidataLabel, String existingName, String countryName) {
        if (wikidataLabel == null || existingName == null) {
            return false;
        }
        String a = normalizeForMatch(wikidataLabel, countryName);
        String b = normalizeForMatch(existingName, countryName);
        if (a.equalsIgnoreCase(b)) {
            return true;
        }
        String looseA = stripTrailingWord(a, "Party", "Parties");
        String looseB = stripTrailingWord(b, "Party", "Parties");
        return !looseA.isEmpty() && looseA.equalsIgnoreCase(looseB);
    }

    private static String normalizeForMatch(String text, String countryName) {
        String result = text.trim();
        if (countryName != null) {
            for (String joiner : new String[]{"of", "for"}) {
                String suffix = " " + joiner + " " + countryName;
                if (result.length() > suffix.length()
                        && result.regionMatches(true, result.length() - suffix.length(), suffix, 0, suffix.length())) {
                    result = result.substring(0, result.length() - suffix.length()).trim();
                    break;
                }
            }
        }
        return Normalizer.normalize(result, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static String stripTrailingWord(String text, String... words) {
        for (String word : words) {
            String suffix = " " + word;
            if (text.length() > suffix.length()
                    && text.regionMatches(true, text.length() - suffix.length(), suffix, 0, suffix.length())) {
                return text.substring(0, text.length() - suffix.length()).trim();
            }
        }
        return text;
    }

    public static boolean applyToParty(PoliticalParty party, NormalizedPartyComposition normalized, Country country) {
        boolean isNew = party.getId() == null;
        boolean changed = false;

        if (!normalized.wikidataPartyId().equals(party.getWikidataPartyId())) {
            party.setWikidataPartyId(normalized.wikidataPartyId());
            changed = true;
        }
        if (party.getCountry() == null || !Objects.equals(party.getCountry().getId(), country.getId())) {
            party.setCountry(country);
            changed = true;
        }
        if (isNew && !normalized.partyLabel().equals(party.getName())) {
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
        election.setImportedAt(now);
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
