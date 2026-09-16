package io.github.selenajiro.europolitik.wikidataimport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class WikidataClient {

    private final RestClient restClient;
    private final String baseUrl;

    public WikidataClient(RestClient.Builder restClientBuilder,
                          @Value("${europolitik.import.wikidata.base-url:https://query.wikidata.org/sparql}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = restClientBuilder
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

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        URI uri = URI.create(baseUrl + "?query=" + encodedQuery);

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(SparqlResultsDto.class);
    }
}
