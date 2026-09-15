package io.github.selenajiro.europolitik.electionimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.electionresult.ElectionResult;
import io.github.selenajiro.europolitik.politicalparty.PoliticalParty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;

public class ParlGovElectionImportMapper {

    private static final String SOURCE_NAME = "ParlGov";
    private static final String SOURCE_URL = "https://www.parlgov.org";

    private static final Map<String, String> ISO3_TO_ISO2 = buildIso3ToIso2Map();

    private ParlGovElectionImportMapper() {
    }

    public record NormalizedRow(
            String countryIsoCode,
            Long parlgovElectionId,
            String electionType,
            LocalDate electionDate,
            Long parlgovPartyId,
            String partyName,
            String partyShortName,
            BigDecimal votePercentage,
            Integer seats
    ) {}

    public static NormalizedRow normalize(ParlGovElectionRow row) {
        if (row == null || isBlank(row.countryNameShort) || isBlank(row.electionType)
                || isBlank(row.electionDate) || isBlank(row.electionId) || isBlank(row.partyId)) {
            return null;
        }

        String isoCode = ISO3_TO_ISO2.get(row.countryNameShort.trim().toUpperCase());
        if (isoCode == null) {
            return null;
        }

        LocalDate electionDate;
        long parlgovElectionId;
        long parlgovPartyId;
        try {
            electionDate = LocalDate.parse(row.electionDate.trim());
            parlgovElectionId = Long.parseLong(row.electionId.trim());
            parlgovPartyId = Long.parseLong(row.partyId.trim());
        } catch (Exception e) {
            return null;
        }

        String partyName = firstNonBlank(row.partyNameEnglish, row.partyName, row.partyNameShort);
        if (partyName == null) {
            return null;
        }

        String type = "ep".equalsIgnoreCase(row.electionType.trim()) ? "EUROPEAN_PARLIAMENT" : "NATIONAL_PARLIAMENT";

        return new NormalizedRow(isoCode, parlgovElectionId, type, electionDate, parlgovPartyId,
                partyName, row.partyNameShort, parseDecimal(row.voteShare), parseInt(row.seats));
    }

    public static String electionName(String countryName, NormalizedRow row) {
        String label = "EUROPEAN_PARLIAMENT".equals(row.electionType()) ? "European Parliament election" : "parliamentary election";
        return countryName + " " + label + " " + row.electionDate().getYear();
    }

    public static boolean applyToParty(PoliticalParty party, NormalizedRow row, Country country) {
        boolean changed = false;
        if (!Objects.equals(party.getParlgovPartyId(), row.parlgovPartyId())) {
            party.setParlgovPartyId(row.parlgovPartyId());
            changed = true;
        }
        if (party.getCountry() == null || !Objects.equals(party.getCountry().getId(), country.getId())) {
            party.setCountry(country);
            changed = true;
        }
        if (!Objects.equals(party.getName(), row.partyName())) {
            party.setName(row.partyName());
            changed = true;
        }
        if (!Objects.equals(party.getShortName(), row.partyShortName())) {
            party.setShortName(row.partyShortName());
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

    public static boolean applyToElection(Election election, NormalizedRow row, Country country) {
        boolean changed = false;
        if (!Objects.equals(election.getParlgovElectionId(), row.parlgovElectionId())) {
            election.setParlgovElectionId(row.parlgovElectionId());
            changed = true;
        }
        if (election.getCountry() == null || !Objects.equals(election.getCountry().getId(), country.getId())) {
            election.setCountry(country);
            changed = true;
        }
        if (!row.electionType().equals(election.getType())) {
            election.setType(row.electionType());
            changed = true;
        }
        if (!Objects.equals(election.getElectionDate(), row.electionDate())) {
            election.setElectionDate(row.electionDate());
            changed = true;
        }
        String name = electionName(country.getName(), row);
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
        if (election.getImportedAt() == null) {
            election.setImportedAt(now);
        }
        if (election.getCreatedAt() == null) {
            election.setCreatedAt(now);
        }
        if (changed) {
            election.setUpdatedAt(now);
        }
        return changed;
    }

    public static boolean applyToResult(ElectionResult result, NormalizedRow row, Election election, PoliticalParty party) {
        boolean changed = false;
        if (result.getElection() == null || !Objects.equals(result.getElection().getId(), election.getId())) {
            result.setElection(election);
            changed = true;
        }
        if (result.getParty() == null || !Objects.equals(result.getParty().getId(), party.getId())) {
            result.setParty(party);
            changed = true;
        }
        if (!Objects.equals(result.getVotePercentage(), row.votePercentage())) {
            result.setVotePercentage(row.votePercentage());
            changed = true;
        }
        if (!Objects.equals(result.getSeats(), row.seats())) {
            result.setSeats(row.seats());
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

    private static Map<String, String> buildIso3ToIso2Map() {
        Map<String, String> map = new HashMap<>();
        for (String iso2 : Locale.getISOCountries()) {
            try {
                map.put(Locale.of("", iso2).getISO3Country(), iso2);
            } catch (MissingResourceException ignored) {
                // No ISO3 code for this entry
            }
        }
        return map;
    }

    private static BigDecimal parseDecimal(String value) {
        if (isBlank(value)) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseInt(String value) {
        if (isBlank(value)) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) return value.trim();
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
