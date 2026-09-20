package io.github.selenajiro.europolitik.rulingpartyimport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Fetches from Wikipedia's MediaWiki API: the ruling-parties wikitext
 * (large, needs its own long timeout -- see fetchRulingPartiesWikitext),
 * plus category lookups and article extracts used by election discovery.
 */
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
                .defaultHeader("User-Agent", "EuroPolitik/1.0 (https://github.com/selenajiro/EuroPolitik;)")
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

    /** Titles of every page in "Category:{year} elections in {countryName}". */
    public List<String> fetchElectionCategoryMembers(String countryName, int year) {
        String categoryTitle = "Category:" + year + " elections in " + countryName;
        String encoded = URLEncoder.encode(categoryTitle, StandardCharsets.UTF_8);
        URI uri = URI.create(baseUrl + "?action=query&list=categorymembers&cmtitle=" + encoded + "&cmlimit=100&format=json");

        CategoryMembersResponseDto response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(CategoryMembersResponseDto.class);

        if (response == null || response.query() == null || response.query().categorymembers() == null) {
            return List.of();
        }
        return response.query().categorymembers().stream()
                .map(CategoryMembersResponseDto.CategoryMember::title)
                .toList();
    }

    /** Plain-text intro paragraph of an article, used to find its date. */
    public String fetchArticleExtract(String articleTitle) {
        String encoded = URLEncoder.encode(articleTitle, StandardCharsets.UTF_8);
        URI uri = URI.create(baseUrl + "?action=query&prop=extracts&exintro=true&explaintext=true&titles="
                + encoded + "&format=json");

        ExtractResponseDto response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(ExtractResponseDto.class);

        if (response == null || response.query() == null || response.query().pages() == null) {
            return null;
        }
        return response.query().pages().values().stream()
                .findFirst()
                .map(ExtractResponseDto.PageExtractDto::extract)
                .orElse(null);
    }
}