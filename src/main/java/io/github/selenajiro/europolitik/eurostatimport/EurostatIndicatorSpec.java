package io.github.selenajiro.europolitik.eurostatimport;

import java.util.Map;

public record EurostatIndicatorSpec(
        String indicatorCode,
        String datasetCode,
        Map<String, String> filters,
        String unit,
        String sourceUrl
) {}
