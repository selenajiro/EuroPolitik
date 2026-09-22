package io.github.selenajiro.europolitik.countrylanguage;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.wikidataimport.SparqlResultsDto.SparqlValue;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CountryLanguageMapper {

    private static final String SOURCE_NAME = "Wikidata";
    private static final String SOURCE_URL = "https://www.wikidata.org";

    private static final Map<String, String> LANGUAGE_FAMILIES = buildFamilyMap();

    private CountryLanguageMapper() {
    }

    public record NormalizedLanguage(String isoCode, String language, boolean official) {}

    /** A (country, language) pair can appear as both "official" and "other" in the raw results -- collapse to one row per pair, preferring official. */
    public static List<NormalizedLanguage> normalize(List<Map<String, SparqlValue>> bindings) {
        Map<String, NormalizedLanguage> byKey = new LinkedHashMap<>();

        for (Map<String, SparqlValue> binding : bindings) {
            SparqlValue isoValue = binding.get("isoCode");
            SparqlValue labelValue = binding.get("languageLabel");
            SparqlValue typeValue = binding.get("type");
            if (isoValue == null || isBlank(isoValue.value())
                    || labelValue == null || isBlank(labelValue.value())
                    || typeValue == null) {
                continue;
            }

            String isoCode = isoValue.value().trim().toUpperCase();
            String language = labelValue.value().trim();
            boolean official = "official".equals(typeValue.value());
            String key = isoCode + "|" + language.toLowerCase();

            NormalizedLanguage existing = byKey.get(key);
            if (existing == null || (official && !existing.official())) {
                byKey.put(key, new NormalizedLanguage(isoCode, language, official));
            }
        }

        return List.copyOf(byKey.values());
    }

    public static String familyFor(String language) {
        return LANGUAGE_FAMILIES.get(language);
    }

    public static boolean applyTo(CountryLanguage entity, NormalizedLanguage normalized, Country country) {
        boolean changed = false;

        if (entity.getCountry() == null || !entity.getCountry().getId().equals(country.getId())) {
            entity.setCountry(country);
            changed = true;
        }
        if (!normalized.language().equals(entity.getLanguage())) {
            entity.setLanguage(normalized.language());
            changed = true;
        }
        if (entity.isOfficial() != normalized.official()) {
            entity.setOfficial(normalized.official());
            changed = true;
        }
        boolean minority = !normalized.official();
        if (entity.isMinority() != minority) {
            entity.setMinority(minority);
            changed = true;
        }
        String family = familyFor(normalized.language());
        if (family != null && !family.equals(entity.getLanguageFamily())) {
            entity.setLanguageFamily(family);
            changed = true;
        }
        if (entity.getSourceName() == null) {
            entity.setSourceName(SOURCE_NAME);
            entity.setSourceUrl(SOURCE_URL);
            changed = true;
        }

        LocalDateTime now = LocalDateTime.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        if (changed) {
            entity.setUpdatedAt(now);
        }
        return changed;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static Map<String, String> buildFamilyMap() {
        Map<String, String> m = new HashMap<>();
        String[][] entries = {
                {"German", "Germanic"}, {"English", "Germanic"}, {"Dutch", "Germanic"},
                {"Swedish", "Germanic"}, {"Danish", "Germanic"}, {"Norwegian", "Germanic"},
                {"Icelandic", "Germanic"}, {"Faroese", "Germanic"}, {"Luxembourgish", "Germanic"},
                {"Frisian", "Germanic"}, {"Low German", "Germanic"}, {"Yiddish", "Germanic"},
                {"French", "Romance"}, {"Italian", "Romance"}, {"Spanish", "Romance"},
                {"Portuguese", "Romance"}, {"Romanian", "Romance"}, {"Catalan", "Romance"},
                {"Galician", "Romance"}, {"Occitan", "Romance"}, {"Corsican", "Romance"},
                {"Sardinian", "Romance"}, {"Romansh", "Romance"}, {"Aromanian", "Romance"},
                {"Friulian", "Romance"}, {"Ladin", "Romance"},
                {"Russian", "Slavic"}, {"Polish", "Slavic"}, {"Czech", "Slavic"},
                {"Slovak", "Slavic"}, {"Ukrainian", "Slavic"}, {"Belarusian", "Slavic"},
                {"Bulgarian", "Slavic"}, {"Serbian", "Slavic"}, {"Croatian", "Slavic"},
                {"Bosnian", "Slavic"}, {"Slovenian", "Slavic"}, {"Macedonian", "Slavic"},
                {"Montenegrin", "Slavic"}, {"Rusyn", "Slavic"}, {"Sorbian", "Slavic"},
                {"Lithuanian", "Baltic"}, {"Latvian", "Baltic"}, {"Latgalian", "Baltic"},
                {"Irish", "Celtic"}, {"Welsh", "Celtic"}, {"Scottish Gaelic", "Celtic"},
                {"Breton", "Celtic"}, {"Cornish", "Celtic"}, {"Manx", "Celtic"},
                {"Finnish", "Uralic"}, {"Estonian", "Uralic"}, {"Hungarian", "Uralic"},
                {"Sami", "Uralic"}, {"Northern Sami", "Uralic"}, {"Karelian", "Uralic"},
                {"Greek", "Hellenic"}, {"Albanian", "Albanian"}, {"Armenian", "Armenian"},
                {"Turkish", "Turkic"}, {"Azerbaijani", "Turkic"}, {"Gagauz", "Turkic"},
                {"Tatar", "Turkic"}, {"Bashkir", "Turkic"}, {"Chuvash", "Turkic"},
                {"Maltese", "Semitic"}, {"Hebrew", "Semitic"}, {"Basque", "Language isolate"},
                {"Georgian", "Kartvelian"}, {"Romani", "Indo-Aryan"}
        };
        for (String[] e : entries) {
            m.put(e[0], e[1]);
        }
        return m;
    }
}
