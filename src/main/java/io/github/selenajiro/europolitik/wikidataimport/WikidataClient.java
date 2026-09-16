package io.github.selenajiro.europolitik.wikidataimport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WikidataClient {

    private final RestClient restClient;

    public WikidataClient(RestClient.Builder restClientBuilder,
                          @Value("${europolitik.import.wikidata.base-url:https://query.wikidata.org/sparql}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "EuroPolitik-portfolio-project/1.0")
                .defaultHeader("Accept", "application/sparql-results+json")
                .build();
    }

    public SparqlResultsDto queryCurrentComposition(String positionQid) {
        String query = """
                SELECT ?party ?partyLabel (COUNT(DISTINCT ?politician) AS ?count) WHERE {
                  ?politician p:P39 ?membership .
                  ?membership ps:P39 wd:%s .
                  FILTER NOT EXISTS { ?membership pq:P582 ?end }
                  ?politician wdt:P102 ?party .
                  SERVICE wikibase:label { bd:serviceParam wikibase:language "en" }
                }
                GROUP BY ?party ?partyLabel
                ORDER BY DESC(?count)
                """.formatted(positionQid);

        return restClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("query", query).build())
                .retrieve()
                .body(SparqlResultsDto.class);
    }
}
