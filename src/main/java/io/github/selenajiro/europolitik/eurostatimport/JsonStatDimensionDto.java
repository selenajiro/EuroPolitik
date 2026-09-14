package io.github.selenajiro.europolitik.eurostatimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonStatDimensionDto(
        String label,
        JsonStatCategoryDto category
) {}
