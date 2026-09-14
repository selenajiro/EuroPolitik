package io.github.selenajiro.europolitik.eurostatimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonStatDto(
        List<String> id,
        List<Integer> size,
        Map<String, JsonStatDimensionDto> dimension,
        Map<String, BigDecimal> value
) {}