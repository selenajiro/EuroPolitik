package io.github.selenajiro.europolitik.mepimport;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A single MEP record from the European Parliament Open Data API
 * (https://data.europarl.europa.eu/api/v2/meps/show-current).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EpMepDto(
        @JsonAlias({"identifier", "id"})
        String identifier,

        @JsonAlias({"label", "name", "fullName", "full-name"})
        String label,

        @JsonAlias({"api:country-of-representation", "country", "country-of-representation", "countryOfRepresentation"})
        String country,

        @JsonAlias({"api:political-group", "politicalGroup", "political-group", "political_group"})
        String politicalGroup,

        @JsonAlias({"mandateStart", "mandate-start"})
        String mandateStart,

        @JsonAlias({"mandateEnd", "mandate-end"})
        String mandateEnd
) {}
