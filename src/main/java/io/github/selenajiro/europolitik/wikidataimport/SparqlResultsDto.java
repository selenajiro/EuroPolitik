package io.github.selenajiro.europolitik.wikidataimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SparqlResultsDto(Results results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Results(List<Map<String, SparqlValue>> bindings) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SparqlValue(String type, String value) {}
}
