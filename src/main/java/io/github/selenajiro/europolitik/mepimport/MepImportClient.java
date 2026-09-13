package io.github.selenajiro.europolitik.mepimport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class MepImportClient {

    private static final int PAGE_SIZE = 50;
    private static final int MAX_PAGES = 50; // safety cap: 50 x 50 = 2500 records

    private final RestClient restClient;

    public MepImportClient(RestClient.Builder restClientBuilder,
                           @Value("${europolitik.import.ep.base-url:https://data.europarl.europa.eu/api/v2}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "EuroPolitik-portfolio-project/1.0")
                .build();
    }

    public List<EpMepDto> fetchCurrentMeps() {
        List<EpMepDto> all = new ArrayList<>();

        for (int page = 0; page < MAX_PAGES; page++) {
            int offset = page * PAGE_SIZE;

            EpMepListResponseDto response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/meps/show-current")
                            .queryParam("format", "application/ld+json")
                            .queryParam("offset", offset)
                            .queryParam("limit", PAGE_SIZE)
                            .build())
                    .retrieve()
                    .body(EpMepListResponseDto.class);

            List<EpMepDto> members = response == null ? List.of() : response.membersOrEmpty();
            all.addAll(members);

            if (members.size() < PAGE_SIZE) {
                break;
            }
        }

        return all;
    }
}
