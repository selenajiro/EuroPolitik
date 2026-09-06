package io.github.selenajiro.europolitik.country;

public record CountryComparisonResponse(
        CountryProfileResponse countryA,
        CountryProfileResponse countryB
) {}
