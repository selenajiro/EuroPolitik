package io.github.selenajiro.europolitik.country;

import io.github.selenajiro.europolitik.countrylanguage.CountryLanguageResponse;
import io.github.selenajiro.europolitik.countrystatistic.CountryStatisticResponse;
import io.github.selenajiro.europolitik.election.ElectionResponse;
import io.github.selenajiro.europolitik.mep.MepResponse;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventResponse;
import io.github.selenajiro.europolitik.politicalparty.PoliticalPartyResponse;

import java.util.List;

public record CountryProfileResponse(
        CountryResponse country,
        List<PoliticalPartyResponse> politicalParties,
        List<ElectionResponse> elections,
        List<MepResponse> meps,
        List<PoliticalEventResponse> politicalEvents,
        List<CountryStatisticResponse> statistics,
        List<CountryLanguageResponse> languages
) {}
