package io.github.selenajiro.europolitik.eurostatimport;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.countrystatistic.CountryStatistic;
import io.github.selenajiro.europolitik.eurostatimport.JsonStatDecoder.Observation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class EurostatImportMapper {

    public static final String INDICATOR = "POPULATION";
    private static final String SOURCE_NAME = "Eurostat";
    private static final String SOURCE_URL = "https://ec.europa.eu/eurostat/databrowser/view/demo_gind/default/table";

    private static final Map<String, String> COUNTRY_CODE_FIXUPS = Map.of(
            "EL", "GR",
            "UK", "GB"
    );

    private EurostatImportMapper() {
    }

    public record NormalizedStatistic(String isoCode, Integer year, BigDecimal value) {}

    public static NormalizedStatistic normalize(Observation observation) {
        if (observation == null) {
            return null;
        }

        String geoCode = observation.code("geo");
        String timeCode = observation.code("time");
        BigDecimal value = observation.value();

        if (isBlank(geoCode) || isBlank(timeCode) || value == null) {
            return null;
        }

        String isoCode = geoCode.trim().toUpperCase();
        isoCode = COUNTRY_CODE_FIXUPS.getOrDefault(isoCode, isoCode);

        Integer year;
        try {
            year = Integer.parseInt(timeCode.trim());
        } catch (NumberFormatException e) {
            return null;
        }

        return new NormalizedStatistic(isoCode, year, value);
    }

    public static boolean applyTo(CountryStatistic stat, NormalizedStatistic normalized, Country country) {
        boolean changed = false;

        if (stat.getCountry() == null || !Objects.equals(stat.getCountry().getId(), country.getId())) {
            stat.setCountry(country);
            changed = true;
        }
        if (!INDICATOR.equals(stat.getIndicator())) {
            stat.setIndicator(INDICATOR);
            changed = true;
        }
        if (!Objects.equals(stat.getYear(), normalized.year())) {
            stat.setYear(normalized.year());
            changed = true;
        }
        if (stat.getValue() == null || stat.getValue().compareTo(normalized.value()) != 0) {
            stat.setValue(normalized.value());
            changed = true;
        }
        if (!"persons".equals(stat.getUnit())) {
            stat.setUnit("persons");
            changed = true;
        }
        if (stat.getSourceName() == null) {
            stat.setSourceName(SOURCE_NAME);
            stat.setSourceUrl(SOURCE_URL);
            changed = true;
        }

        LocalDateTime now = LocalDateTime.now();
        if (stat.getCreatedAt() == null) {
            stat.setCreatedAt(now);
        }
        if (changed) {
            stat.setUpdatedAt(now);
        }

        return changed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
