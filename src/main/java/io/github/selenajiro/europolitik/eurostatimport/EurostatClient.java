package io.github.selenajiro.europolitik.eurostatimport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class EurostatClient {

    private final RestClient restClient;

    public EurostatClient(RestClient.Builder restClientBuilder,
                          @Value("${europolitik.import.eurostat.base-url:https://ec.europa.eu/eurostat/api/dissemination/statistics/1.0/data}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "EuroPolitik-portfolio-project/1.0")
                .build();
    }

    public JsonStatDto fetchPopulation() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/demo_gind")
                        .queryParam("format", "JSON")
                        .queryParam("lang", "EN")
                        .queryParam("indic_de", "JAN")
                        .queryParam("sinceTimePeriod", "2011")
                        .build())
                .retrieve()
                .body(JsonStatDto.class);
    }
}
