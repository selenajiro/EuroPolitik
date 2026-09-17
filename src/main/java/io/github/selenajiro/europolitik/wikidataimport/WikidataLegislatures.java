package io.github.selenajiro.europolitik.wikidataimport;

import java.util.List;

/**
 * Countries currently covered by the Wikidata current-composition
 * importer. Verified working: these three consistently mark current
 * members with wikibase:PreferredRank. Italy, Netherlands, Sweden,
 * Belgium and Spain were tried and consistently return empty results --
 * not a bug, those legislatures just don't use the same rank
 * convention on Wikidata. Not pursuing per-country fixes for those;
 * add a country back here only once confirmed with real data.
 */
public class WikidataLegislatures {

    public static final List<WikidataLegislatureSpec> SPECS = List.of(
            new WikidataLegislatureSpec("DE", "Q1939555"),   // Member of the Bundestag
            new WikidataLegislatureSpec("PL", "Q19269361"),  // Member of the Sejm
            new WikidataLegislatureSpec("FR", "Q3044918")    // Member of the French National Assembly
    );

    private WikidataLegislatures() {
    }
}
