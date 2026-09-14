package io.github.selenajiro.europolitik.eurostatimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonStatCategoryDto(
        Map<String, Integer> index,
        Map<String, String> label
) {}
