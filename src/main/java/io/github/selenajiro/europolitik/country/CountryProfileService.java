package io.github.selenajiro.europolitik.country;

import io.github.selenajiro.europolitik.countrylanguage.CountryLanguageResponse;
import io.github.selenajiro.europolitik.countrylanguage.CountryLanguageService;
import io.github.selenajiro.europolitik.countrystatistic.CountryStatisticResponse;
import io.github.selenajiro.europolitik.countrystatistic.CountryStatisticService;
import io.github.selenajiro.europolitik.election.ElectionResponse;
import io.github.selenajiro.europolitik.election.ElectionService;
import io.github.selenajiro.europolitik.mep.MepResponse;
import io.github.selenajiro.europolitik.mep.MepService;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventResponse;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventService;
import io.github.selenajiro.europolitik.politicalparty.PoliticalPartyResponse;
import io.github.selenajiro.europolitik.politicalparty.PoliticalPartyService;
import org.springframework.stereotype.Service;

@Service
public class CountryProfileService {

    private final CountryService countryService;
    private final PoliticalPartyService politicalPartyService;
    private final ElectionService electionService;
    private final MepService mepService;
    private final PoliticalEventService politicalEventService;
    private final CountryStatisticService countryStatisticService;
    private final CountryLanguageService countryLanguageService;

    public CountryProfileService(CountryService countryService,
                                 PoliticalPartyService politicalPartyService,
                                 ElectionService electionService,
                                 MepService mepService,
                                 PoliticalEventService politicalEventService,
                                 CountryStatisticService countryStatisticService,
                                 CountryLanguageService countryLanguageService) {
        this.countryService = countryService;
        this.politicalPartyService = politicalPartyService;
        this.electionService = electionService;
        this.mepService = mepService;
        this.politicalEventService = politicalEventService;
        this.countryStatisticService = countryStatisticService;
        this.countryLanguageService = countryLanguageService;
    }

    public CountryProfileResponse buildProfile(Long countryId) {
        Country country = countryService.findById(countryId);

        return new CountryProfileResponse(
                CountryResponse.from(country),
                politicalPartyService.findAllByCountryId(countryId).stream().map(PoliticalPartyResponse::from).toList(),
                electionService.findAllByCountryId(countryId).stream().map(ElectionResponse::from).toList(),
                mepService.findAllByCountryId(countryId).stream().map(MepResponse::from).toList(),
                politicalEventService.findAllByCountryId(countryId).stream().map(PoliticalEventResponse::from).toList(),
                countryStatisticService.findAllByCountryId(countryId).stream().map(CountryStatisticResponse::from).toList(),
                countryLanguageService.findAllByCountryId(countryId).stream().map(CountryLanguageResponse::from).toList()
        );
    }
}
