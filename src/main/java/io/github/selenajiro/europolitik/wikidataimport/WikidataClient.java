package io.github.selenajiro.europolitik.wikidataimport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(30_000);

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "EuroPolitik-portfolio-project/1.0")
                .defaultHeader("Accept", "application/sparql-results+json")
                .build();
    }

    public SparqlResultsDto queryCurrentComposition(String positionQid) {
        String query = """
                SELECT ?party ?partyLabel (COUNT(DISTINCT ?politician) AS ?count) WHERE {
                  ?politician p:P39 ?membership .
                  ?membership ps:P39 wd:%s .
                  ?membership wikibase:rank wikibase:PreferredRank .
                  ?politician wdt:P102 ?party .
                  SERVICE wikibase:label { bd:serviceParam wikibase:language "en" }
                }
                GROUP BY ?party ?partyLabel
                ORDER BY DESC(?count)
                """.formatted(positionQid);

        return execute(query);
    }

    public SparqlResultsDto queryLanguages() {
        String query = """
                SELECT ?isoCode ?languageLabel ?type WHERE {
                  {
                    ?country wdt:P37 ?language .
                    BIND("official" AS ?type)
                  }
                  UNION
                  {
                    ?country wdt:P2936 ?language .
                    BIND("other" AS ?type)
                  }
                  ?country wdt:P297 ?isoCode .
                  SERVICE wikibase:label { bd:serviceParam wikibase:language "en" }
                }
                ORDER BY ?isoCode ?type
                """;

        return execute(query);
    }

    private SparqlResultsDto execute(String query) {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        URI uri = URI.create(baseUrl + "?query=" + encodedQuery);

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(SparqlResultsDto.class);
    }
}