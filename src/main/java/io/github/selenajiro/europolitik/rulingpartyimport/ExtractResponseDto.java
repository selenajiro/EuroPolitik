package io.github.selenajiro.europolitik.rulingpartyimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/** Response shape of MediaWiki's action=query&prop=extracts. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtractResponseDto(QueryResult query) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QueryResult(Map<String, PageExtractDto> pages) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PageExtractDto(long pageid, String title, String extract) {}
}
