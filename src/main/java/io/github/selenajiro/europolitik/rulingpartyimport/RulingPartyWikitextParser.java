package io.github.selenajiro.europolitik.rulingpartyimport;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RulingPartyWikitextParser {

    private static final Pattern FLAG_LINK = Pattern.compile("\\{\\{flag\\+link\\|[^|}]*\\|([^|}]+)");
    private static final Pattern HR_SPLIT = Pattern.compile("<hr\\s*/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern REF_TAG = Pattern.compile("<ref[^>]*>.*?</ref>", Pattern.DOTALL);
    private static final Pattern SELF_CLOSING_REF = Pattern.compile("<ref[^>]*/>");
    private static final Pattern INNERMOST_TEMPLATE = Pattern.compile("\\{\\{([^{}]*)}}");
    private static final Pattern WIKILINK = Pattern.compile("\\[\\[([^\\]|]+)(?:\\|([^\\]]+))?]]");
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private static final int MAX_LENGTH = 1000;

    private RulingPartyWikitextParser() {
    }

    public static Map<String, String> parse(String wikitext) {
        Map<String, String> result = new LinkedHashMap<>();
        if (wikitext == null) {
            return result;
        }

        List<String> cells = new ArrayList<>();
        boolean inTable = false;

        for (String rawLine : wikitext.split("\n")) {
            String line = rawLine.trim();

            if (line.startsWith("{|")) {
                inTable = true;
                cells = new ArrayList<>();
                continue;
            }
            if (!inTable) {
                continue;
            }
            if (line.startsWith("|-")) {
                finalizeRow(cells, result);
                cells = new ArrayList<>();
                continue;
            }
            if (line.startsWith("|}")) {
                finalizeRow(cells, result);
                cells = new ArrayList<>();
                inTable = false;
                continue;
            }
            if (line.startsWith("!")) {
                continue;
            }
            if (line.startsWith("|")) {
                cells.add(line.substring(1));
            }
        }
        finalizeRow(cells, result);

        return result;
    }

    private static void finalizeRow(List<String> cells, Map<String, String> result) {
        if (cells.size() != 4) {
            return;
        }

        String countryCell = cells.get(0);

        if (countryCell.contains("''")) {
            return;
        }

        Matcher flagMatcher = FLAG_LINK.matcher(countryCell);
        if (!flagMatcher.find()) {
            return;
        }
        String country = flagMatcher.group(1).trim();
        if (country.isEmpty()) {
            return;
        }

        String rulingRaw = HR_SPLIT.split(stripCellAttributes(cells.get(2)), 2)[0];
        String ruling = clean(rulingRaw);
        if (ruling.isEmpty()) {
            return;
        }
        if (ruling.length() > MAX_LENGTH) {
            ruling = ruling.substring(0, MAX_LENGTH);
        }

        result.put(country, ruling);
    }

    private static String stripCellAttributes(String cell) {
        int pipe = cell.indexOf('|');
        if (pipe < 0) {
            return cell;
        }
        String prefix = cell.substring(0, pipe);
        if (prefix.contains("=") && !prefix.contains("[[") && !prefix.contains("{{")) {
            return cell.substring(pipe + 1);
        }
        return cell;
    }

    private static String clean(String raw) {
        String text = raw;
        text = REF_TAG.matcher(text).replaceAll("");
        text = SELF_CLOSING_REF.matcher(text).replaceAll("");
        text = resolveTemplates(text);
        text = resolveWikilinks(text);
        text = text.replace("'''", "").replace("''", "");
        text = HTML_TAG.matcher(text).replaceAll("");
        text = MULTI_SPACE.matcher(text).replaceAll(" ");
        return text.trim().replaceAll("^[,;\\s]+|[,;\\s]+$", "");
    }

    private static String resolveTemplates(String text) {
        String current = text;
        String previous;
        do {
            previous = current;
            Matcher matcher = INNERMOST_TEMPLATE.matcher(current);
            StringBuilder sb = new StringBuilder();
            while (matcher.find()) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(resolveOneTemplate(matcher.group(1))));
            }
            matcher.appendTail(sb);
            current = sb.toString();
        } while (!current.equals(previous));
        return current;
    }

    private static String resolveOneTemplate(String inner) {
        String[] parts = inner.split("\\|");
        if (parts.length == 0) {
            return "";
        }
        String name = parts[0].trim().toLowerCase();

        if (name.equals("abbr")) {
            return parts.length > 1 ? parts[1] : "";
        }
        if (name.equals("ill")) {
            for (String part : parts) {
                if (part.startsWith("lt=")) {
                    return part.substring(3);
                }
            }
            return parts.length > 1 ? parts[1] : "";
        }
        return "";
    }

    private static String resolveWikilinks(String text) {
        Matcher matcher = WIKILINK.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String display = matcher.group(2) != null ? matcher.group(2) : matcher.group(1);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(display));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}