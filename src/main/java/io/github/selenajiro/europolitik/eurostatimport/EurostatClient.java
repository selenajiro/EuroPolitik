package io.github.selenajiro.europolitik.eurostatimport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EurostatClient {

    private final RestClient restClient;

    public EurostatClient(RestClient.Builder restClientBuilder,
                          @Value("${europolitik.import.eurostat.base-url:https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data}") String baseUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(30_000);

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "EuroPolitik-portfolio-project/1.0")
                .build();
    }

    public JsonStatDto fetch(EurostatIndicatorSpec spec) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/" + spec.datasetCode())
                            .queryParam("format", "JSON")
                            .queryParam("lang", "EN");
                    spec.filters().forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .body(JsonStatDto.class);
    }
}