package io.github.selenajiro.europolitik.rulingpartyimport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class WikipediaClient {

    private static final String PAGE_TITLE = "List of ruling political parties by country";

    private final RestClient restClient;
    private final String baseUrl;

    public WikipediaClient(RestClient.Builder restClientBuilder,
                           @Value("${europolitik.import.wikipedia.base-url:https://en.wikipedia.org/w/api.php}") String baseUrl) {
        this.baseUrl = baseUrl;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(60_000);

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "EuroPolitik-portfolio-project/1.0")
                .build();
    }

    public String fetchRulingPartiesWikitext() {
        String encodedTitle = URLEncoder.encode(PAGE_TITLE, StandardCharsets.UTF_8);
        URI uri = URI.create(baseUrl + "?action=parse&page=" + encodedTitle + "&format=json&prop=wikitext");

        MediaWikiParseResponseDto response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(MediaWikiParseResponseDto.class);

        if (response == null || response.parse() == null || response.parse().wikitext() == null) {
            return null;
        }
        return response.parse().wikitext().content();
    }
}