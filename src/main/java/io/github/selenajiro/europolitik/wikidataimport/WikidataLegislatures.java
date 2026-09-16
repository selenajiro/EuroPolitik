package io.github.selenajiro.europolitik.wikidataimport;

import java.util.List;

public class WikidataLegislatures {

    public static final List<WikidataLegislatureSpec> SPECS = List.of(
            new WikidataLegislatureSpec("DE", "Q1939555"),   // Member of the Bundestag
            new WikidataLegislatureSpec("PL", "Q19269361"),  // Member of the Sejm
            new WikidataLegislatureSpec("FR", "Q3044918"),   // Member of the French National Assembly
            new WikidataLegislatureSpec("IT", "Q18558478"),  // Member of the Chamber of Deputies of the Italian Republic
            new WikidataLegislatureSpec("NL", "Q18887908"),  // Member of the House of Representatives of the Netherlands
            new WikidataLegislatureSpec("SE", "Q10655178"),  // Member of the Swedish Riksdag
            new WikidataLegislatureSpec("BE", "Q15705021"),  // Member of the Chamber of Representatives of Belgium
            new WikidataLegislatureSpec("ES", "Q12156127")   // Member of the Congress of Deputies (Spain) -- least certain, watch this one
    );

    private WikidataLegislatures() {
    }
}
