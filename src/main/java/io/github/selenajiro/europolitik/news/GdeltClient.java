package io.github.selenajiro.europolitik.news;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GdeltClient {

    private final RestClient restClient;
    private final String baseUrl;

    public GdeltClient(RestClient.Builder restClientBuilder,
                       @Value("${europolitik.import.gdelt.base-url:https://api.gdeltproject.org/api/v2/doc/doc}") String baseUrl) {
        this.baseUrl = baseUrl;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(30_000);

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "EuroPolitik-portfolio-project/1.0 (https://github.com/selenajiro/EuroPolitik)")
                .build();
    }

    public String searchNewsRaw(String fipsCountryCode, String keywordFilter, int maxRecords) {
        String query = "sourcecountry:" + fipsCountryCode + " " + keywordFilter;
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        URI uri = URI.create(baseUrl + "?query=" + encodedQuery
                + "&mode=artlist&format=json&maxrecords=" + maxRecords
                + "&timespan=7d&sort=DateDesc");

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }
}
