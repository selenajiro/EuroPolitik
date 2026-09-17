package io.github.selenajiro.europolitik.rulingpartyimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MediaWikiParseResponseDto(ParseResult parse) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParseResult(WikitextDto wikitext) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WikitextDto(@JsonProperty("*") String content) {}
}
