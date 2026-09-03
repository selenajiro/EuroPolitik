package io.github.selenajiro.europolitik.country;

import java.util.List;

public record CountryNeighborsResponse(
        List<NeighborSummary> neighborsInDataset,
        List<String> otherRealWorldNeighbors
) {
    public record NeighborSummary(Long id, String isoCode, String name) {
        public static NeighborSummary from(Country country) {
            return new NeighborSummary(country.getId(), country.getIsoCode(), country.getName());
        }
    }
}
