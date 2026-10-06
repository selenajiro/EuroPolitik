package io.github.selenajiro.europolitik.news;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EuRssClient {

    private final RestClient restClient;
    private final String feedUrl;

    public EuRssClient(RestClient.Builder restClientBuilder,
                       @Value("${europolitik.import.eu-rss.council-url:https://www.consilium.europa.eu/en/rss/pressreleases.ashx}") String feedUrl) {
        this.feedUrl = feedUrl;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(20_000);

        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "EuroPolitik-portfolio-project/1.0 (https://github.com/selenajiro/EuroPolitik)")
                .build();
    }

    public String fetchRaw() {
        return restClient.get()
                .uri(feedUrl)
                .retrieve()
                .body(String.class);
    }
}