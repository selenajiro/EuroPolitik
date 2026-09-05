package io.github.selenajiro.europolitik.savedcomparison;

import jakarta.validation.constraints.NotNull;

public record SaveComparisonRequest(
        @NotNull Long countryAId,
        @NotNull Long countryBId
) {}
