package io.github.selenajiro.europolitik.politicalleaning;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.country.CountryRepository;
import io.github.selenajiro.europolitik.importrun.ImportRun;
import io.github.selenajiro.europolitik.importrun.ImportRunRepository;
import io.github.selenajiro.europolitik.politicalparty.PoliticalParty;
import io.github.selenajiro.europolitik.politicalparty.PoliticalPartyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class PoliticalLeaningService {

    private static final Logger log = LoggerFactory.getLogger(PoliticalLeaningService.class);
    private static final String SOURCE = "derived-political-leaning";
    private static final Pattern TOKEN_SPLIT = Pattern.compile("[,()/]");
    private static final Pattern TRAILING_PAREN = Pattern.compile("\\s*\\([^)]*\\)\\s*$");
    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}");
    private static final String[] COUNTRY_SUFFIX_JOINERS = { "of", "for" };
    private static final String[] GENERIC_TRAILING_WORDS = { "Party", "Parties" };

    private final CountryRepository countryRepository;
    private final PoliticalPartyRepository politicalPartyRepository;
    private final ImportRunRepository importRunRepository;

    public PoliticalLeaningService(CountryRepository countryRepository,
                                   PoliticalPartyRepository politicalPartyRepository,
                                   ImportRunRepository importRunRepository) {
        this.countryRepository = countryRepository;
        this.politicalPartyRepository = politicalPartyRepository;
        this.importRunRepository = importRunRepository;
    }

    @Transactional
    public ImportRun computeLeanings() {
        ImportRun run = startRun();

        int updated = 0;
        int skipped = 0;

        try {
            List<Country> countries = countryRepository.findAll();
            run.setRecordsRead(countries.size());

            for (Country country : countries) {
                String rulingParty = country.getCurrentRulingParty();
                if (rulingParty == null || rulingParty.isBlank()) {
                    skipped++;
                    continue;
                }

                List<PoliticalParty> parties = politicalPartyRepository.findAllByCountryId(country.getId());
                List<String> tokens = tokenize(rulingParty);

                BigDecimal sum = BigDecimal.ZERO;
                int matches = 0;
                Set<Long> matchedPartyIds = new HashSet<>();
                List<String> matchedNames = new ArrayList<>();

                for (String token : tokens) {
                    String normalizedToken = normalize(token, country.getName());
                    String looseToken = stripTrailingWord(normalizedToken, GENERIC_TRAILING_WORDS);

                    for (PoliticalParty party : parties) {
                        if (party.getLeftRight() == null || matchedPartyIds.contains(party.getId())) {
                            continue;
                        }
                        String normalizedShort = normalize(party.getShortName(), country.getName());
                        String normalizedName = normalize(party.getName(), country.getName());

                        boolean matched = normalizedToken.equalsIgnoreCase(normalizedShort)
                                || normalizedToken.equalsIgnoreCase(normalizedName);

                        if (!matched && !looseToken.isEmpty()) {
                            String looseName = stripTrailingWord(normalizedName, GENERIC_TRAILING_WORDS);
                            String looseShort = stripTrailingWord(normalizedShort, GENERIC_TRAILING_WORDS);
                            matched = looseToken.equalsIgnoreCase(looseName) || looseToken.equalsIgnoreCase(looseShort);
                        }

                        if (matched) {
                            sum = sum.add(party.getLeftRight());
                            matches++;
                            matchedPartyIds.add(party.getId());
                            matchedNames.add(party.getName());
                            break;
                        }
                    }
                }

                if (matches == 0) {
                    skipped++;
                    continue;
                }

                BigDecimal average = sum.divide(BigDecimal.valueOf(matches), 1, RoundingMode.HALF_UP);
                String label = toLabel(average);
                country.setCurrentPoliticalLeaning(average);
                country.setCurrentPoliticalLeaningLabel(label);
                country.setPoliticalLeaningComputedAt(LocalDateTime.now());
                countryRepository.save(country);
                updated++;
                log.info("Political leaning for {}: {} ({}, matched: {})",
                        country.getName(), average, label, String.join(", ", matchedNames));
            }

            finishRun(run, "SUCCESS", 0, updated, skipped, null);
        } catch (Exception e) {
            log.error("Political leaning computation failed", e);
            finishRun(run, "FAILED", 0, updated, skipped, e.getMessage());
        }

        return run;
    }

    private List<String> tokenize(String text) {
        return Arrays.stream(TOKEN_SPLIT.split(text))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    private String normalize(String text, String countryName) {
        if (text == null) {
            return "";
        }
        String result = text.trim();

        if (countryName != null && result.length() > countryName.length()
                && result.regionMatches(true, 0, countryName, 0, countryName.length())
                && result.charAt(countryName.length()) == ' ') {
            result = result.substring(countryName.length() + 1).trim();
        }

        result = TRAILING_PAREN.matcher(result).replaceAll("").trim();

        // Wikipedia article/link text for a party is often disambiguated
        // with the country name ("Socialist Party of Albania", "Democrats
        // for Andorra"); ParlGov's per-country rows normally don't repeat
        // it. Strip a trailing " of/for {Country}" if present.
        if (countryName != null) {
            for (String joiner : COUNTRY_SUFFIX_JOINERS) {
                String suffix = " " + joiner + " " + countryName;
                if (result.length() > suffix.length()
                        && result.regionMatches(true, result.length() - suffix.length(), suffix, 0, suffix.length())) {
                    result = result.substring(0, result.length() - suffix.length()).trim();
                    break;
                }
            }
        }

        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = COMBINING_MARKS.matcher(result).replaceAll("");

        return result;
    }

    private String stripTrailingWord(String text, String... words) {
        for (String word : words) {
            String suffix = " " + word;
            if (text.length() > suffix.length()
                    && text.regionMatches(true, text.length() - suffix.length(), suffix, 0, suffix.length())) {
                return text.substring(0, text.length() - suffix.length()).trim();
            }
        }
        return text;
    }

    private String toLabel(BigDecimal score) {
        double v = score.doubleValue();
        if (v < 1.5) return "Far Left";
        if (v < 3.5) return "Left";
        if (v < 4.5) return "Center-Left";
        if (v < 5.5) return "Center";
        if (v < 6.5) return "Center-Right";
        if (v < 8.5) return "Right";
        return "Far Right";
    }

    private ImportRun startRun() {
        ImportRun run = new ImportRun();
        run.setSource(SOURCE);
        run.setStartedAt(LocalDateTime.now());
        run.setCreatedAt(LocalDateTime.now());
        run.setStatus("RUNNING");
        run.setRecordsRead(0);
        run.setRecordsInserted(0);
        run.setRecordsUpdated(0);
        run.setRecordsSkipped(0);
        return importRunRepository.save(run);
    }

    private void finishRun(ImportRun run, String status, int inserted, int updated, int skipped, String errorMessage) {
        run.setStatus(status);
        run.setFinishedAt(LocalDateTime.now());
        run.setRecordsInserted(inserted);
        run.setRecordsUpdated(updated);
        run.setRecordsSkipped(skipped);
        run.setErrorMessage(errorMessage);
        importRunRepository.save(run);
    }
}