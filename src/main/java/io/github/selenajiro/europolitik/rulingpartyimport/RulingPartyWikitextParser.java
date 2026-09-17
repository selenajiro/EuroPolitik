package io.github.selenajiro.europolitik.rulingpartyimport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RulingPartyWikitextParser {

    private static final Pattern WIKILINK = Pattern.compile("\\[\\[([^\\]|]+)(?:\\|([^\\]]+))?]]");
    private static final Pattern REF_TAG = Pattern.compile("<ref[^>]*>.*?</ref>", Pattern.DOTALL);
    private static final Pattern SELF_CLOSING_REF = Pattern.compile("<ref[^>]*/>");
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern TEMPLATE = Pattern.compile("\\{\\{[^{}]*}}");
    private static final Pattern CITATION_BRACKET = Pattern.compile("\\[\\d+]");
    private static final Pattern BR_TAG = Pattern.compile("<br\\s*/?>", Pattern.CASE_INSENSITIVE);

    private RulingPartyWikitextParser() {
    }

    /** Returns countryName -> rulingPartyText for every data row found. */
    public static Map<String, String> parse(String wikitext) {
        Map<String, String> result = new LinkedHashMap<>();
        if (wikitext == null) {
            return result;
        }

        List<String> currentRowCells = new ArrayList<>();
        boolean inRow = false;

        for (String rawLine : wikitext.split("\n")) {
            String line = rawLine.trim();

            if (line.startsWith("|-")) {
                finalizeRow(currentRowCells, result);
                currentRowCells = new ArrayList<>();
                inRow = true;
                continue;
            }
            if (line.startsWith("|}")) {
                finalizeRow(currentRowCells, result);
                currentRowCells = new ArrayList<>();
                inRow = false;
                continue;
            }
            if (line.startsWith("!") || !inRow) {
                continue;
            }
            if (line.startsWith("|")) {
                for (String cell : line.substring(1).split("\\|\\|")) {
                    currentRowCells.add(cell);
                }
            }
        }
        finalizeRow(currentRowCells, result);

        return result;
    }

    private static void finalizeRow(List<String> cells, Map<String, String> result) {
        if (cells.size() < 3) {
            return;
        }

        String country = clean(cells.get(0));
        if (country.isBlank()) {
            return;
        }

        String rulingRaw;
        if (cells.size() >= 5) {
            rulingRaw = cells.get(2);
        } else {
            String[] split = BR_TAG.split(cells.get(2), 2);
            rulingRaw = split[0];
        }

        String ruling = clean(rulingRaw);
        if (!ruling.isBlank()) {
            result.put(country, ruling);
        }
    }

    private static String clean(String raw) {
        if (raw == null) {
            return "";
        }
        String text = raw;
        text = REF_TAG.matcher(text).replaceAll("");
        text = SELF_CLOSING_REF.matcher(text).replaceAll("");
        text = CITATION_BRACKET.matcher(text).replaceAll("");

        Matcher wikilinkMatcher = WIKILINK.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (wikilinkMatcher.find()) {
            String display = wikilinkMatcher.group(2) != null ? wikilinkMatcher.group(2) : wikilinkMatcher.group(1);
            wikilinkMatcher.appendReplacement(sb, Matcher.quoteReplacement(display));
        }
        wikilinkMatcher.appendTail(sb);
        text = sb.toString();

        text = TEMPLATE.matcher(text).replaceAll("");
        text = HTML_TAG.matcher(text).replaceAll("");
        return text.trim();
    }
}
