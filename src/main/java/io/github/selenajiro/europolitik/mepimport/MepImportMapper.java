package io.github.selenajiro.europolitik.mepimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.mep.Mep;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class MepImportMapper {

    private static final String CURRENT_TERM_LABEL = "10th (2024-2029)";
    private static final String SOURCE_NAME = "European Parliament Open Data Portal";
    private static final String SOURCE_URL = "https://data.europarl.europa.eu/api/v2/meps/show-current";

    // EP political group codes -> full names, 10th parliamentary term.
    private static final Map<String, String> POLITICAL_GROUPS = Map.ofEntries(
            Map.entry("PPE", "European People's Party (Christian Democrats)"),
            Map.entry("S&D", "Progressive Alliance of Socialists and Democrats"),
            Map.entry("RENEW", "Renew Europe"),
            Map.entry("Verts/ALE", "Greens/European Free Alliance"),
            Map.entry("ECR", "European Conservatives and Reformists"),
            Map.entry("THE-LEFT", "The Left in the European Parliament - GUE/NGL"),
            Map.entry("PfE", "Patriots for Europe"),
            Map.entry("ESN", "Europe of Sovereign Nations"),
            Map.entry("NI", "Non-attached Members"),
            Map.entry("ID", "Identity and Democracy (defunct since July 2024)")
    );

    // The EP API uses "UK" for the United Kingdom; our Country table uses
    // the ISO 3166-1 alpha-2 code "GB".
    private static final Map<String, String> COUNTRY_CODE_FIXUPS = Map.of("UK", "GB");

    private MepImportMapper() {
    }

    public record NormalizedMep(
            Long epMemberId,
            String fullName,
            String isoCode,
            String politicalGroup,
            LocalDate startDate,
            LocalDate endDate
    ) {}

    public static NormalizedMep normalize(EpMepDto raw) {
        if (raw == null || isBlank(raw.identifier()) || isBlank(raw.label()) || isBlank(raw.country())) {
            return null;
        }

        long epMemberId;
        try {
            epMemberId = Long.parseLong(raw.identifier().trim());
        } catch (NumberFormatException e) {
            return null;
        }

        String isoCode = raw.country().trim().toUpperCase();
        isoCode = COUNTRY_CODE_FIXUPS.getOrDefault(isoCode, isoCode);

        String politicalGroup = isBlank(raw.politicalGroup())
                ? null
                : POLITICAL_GROUPS.getOrDefault(raw.politicalGroup().trim(), raw.politicalGroup().trim());

        return new NormalizedMep(
                epMemberId,
                raw.label().trim(),
                isoCode,
                politicalGroup,
                parseDate(raw.mandateStart()),
                parseDate(raw.mandateEnd())
        );
    }

    public static boolean applyTo(Mep mep, NormalizedMep normalized, Country country) {
        boolean changed = false;

        if (!Objects.equals(mep.getEpMemberId(), normalized.epMemberId())) {
            mep.setEpMemberId(normalized.epMemberId());
            changed = true;
        }
        if (!Objects.equals(mep.getFullName(), normalized.fullName())) {
            mep.setFullName(normalized.fullName());
            changed = true;
        }
        if (mep.getCountry() == null || !Objects.equals(mep.getCountry().getId(), country.getId())) {
            mep.setCountry(country);
            changed = true;
        }
        if (!Objects.equals(mep.getPoliticalGroup(), normalized.politicalGroup())) {
            mep.setPoliticalGroup(normalized.politicalGroup());
            changed = true;
        }
        if (!Objects.equals(mep.getStartDate(), normalized.startDate())) {
            mep.setStartDate(normalized.startDate());
            changed = true;
        }
        if (!Objects.equals(mep.getEndDate(), normalized.endDate())) {
            mep.setEndDate(normalized.endDate());
            changed = true;
        }
        if (!CURRENT_TERM_LABEL.equals(mep.getParliamentaryTerm())) {
            mep.setParliamentaryTerm(CURRENT_TERM_LABEL);
            changed = true;
        }
        if (mep.getSourceName() == null) {
            mep.setSourceName(SOURCE_NAME);
            mep.setSourceUrl(SOURCE_URL);
            changed = true;
        }

        LocalDateTime now = LocalDateTime.now();
        if (mep.getCreatedAt() == null) {
            mep.setCreatedAt(now);
        }
        if (changed) {
            mep.setUpdatedAt(now);
        }

        return changed;
    }

    private static LocalDate parseDate(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}