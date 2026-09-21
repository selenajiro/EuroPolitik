package io.github.selenajiro.europolitik.country;

import io.github.selenajiro.europolitik.countrylanguage.CountryLanguageResponse;
import io.github.selenajiro.europolitik.countrylanguage.CountryLanguageService;
import io.github.selenajiro.europolitik.countrystatistic.CountryStatisticResponse;
import io.github.selenajiro.europolitik.countrystatistic.CountryStatisticService;
import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.election.ElectionRepository;
import io.github.selenajiro.europolitik.election.ElectionResponse;
import io.github.selenajiro.europolitik.election.ElectionService;
import io.github.selenajiro.europolitik.electionresult.ElectionResultRepository;
import io.github.selenajiro.europolitik.electionresult.PartySeatsResponse;
import io.github.selenajiro.europolitik.mep.MepResponse;
import io.github.selenajiro.europolitik.mep.MepService;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventResponse;
import io.github.selenajiro.europolitik.politicalevent.PoliticalEventService;
import io.github.selenajiro.europolitik.politicalparty.PoliticalPartyResponse;
import io.github.selenajiro.europolitik.politicalparty.PoliticalPartyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CountryProfileService {

    private static final String COMPOSITION_TYPE = "CURRENT_COMPOSITION";

    private final CountryService countryService;
    private final PoliticalPartyService politicalPartyService;
    private final ElectionService electionService;
    private final ElectionRepository electionRepository;
    private final ElectionResultRepository electionResultRepository;
    private final MepService mepService;
    private final PoliticalEventService politicalEventService;
    private final CountryStatisticService countryStatisticService;
    private final CountryLanguageService countryLanguageService;

    public CountryProfileService(CountryService countryService,
                                 PoliticalPartyService politicalPartyService,
                                 ElectionService electionService,
                                 ElectionRepository electionRepository,
                                 ElectionResultRepository electionResultRepository,
                                 MepService mepService,
                                 PoliticalEventService politicalEventService,
                                 CountryStatisticService countryStatisticService,
                                 CountryLanguageService countryLanguageService) {
        this.countryService = countryService;
        this.politicalPartyService = politicalPartyService;
        this.electionService = electionService;
        this.electionRepository = electionRepository;
        this.electionResultRepository = electionResultRepository;
        this.mepService = mepService;
        this.politicalEventService = politicalEventService;
        this.countryStatisticService = countryStatisticService;
        this.countryLanguageService = countryLanguageService;
    }

    public CountryProfileResponse buildProfile(Long countryId) {
        CountryResponse country = countryService.findByIdProjected(countryId);
        Double areaSquareKm = countryService.findAreaSquareKm(countryId);

        List<ElectionResponse> elections = electionService.findAllByCountryId(countryId).stream()
                .filter(e -> !COMPOSITION_TYPE.equals(e.getType()))
                .map(ElectionResponse::from)
                .toList();

        List<PartySeatsResponse> currentComposition = findCompositionElection(countryId)
                .map(election -> electionResultRepository.findByElectionId(election.getId()).stream()
                        .map(PartySeatsResponse::from)
                        .toList())
                .orElse(List.of());

        return new CountryProfileResponse(
                country,
                areaSquareKm,
                politicalPartyService.findAllByCountryId(countryId).stream().map(PoliticalPartyResponse::from).toList(),
                elections,
                mepService.findAllByCountryId(countryId).stream().map(MepResponse::from).toList(),
                politicalEventService.findAllByCountryId(countryId).stream().map(PoliticalEventResponse::from).toList(),
                countryStatisticService.findAllByCountryId(countryId).stream().map(CountryStatisticResponse::from).toList(),
                countryLanguageService.findAllByCountryId(countryId).stream().map(CountryLanguageResponse::from).toList(),
                currentComposition
        );
    }

    private Optional<Election> findCompositionElection(Long countryId) {
        return electionRepository.findByCountryIdAndType(countryId, COMPOSITION_TYPE);
    }

    public CountryComparisonResponse compare(Long idA, Long idB) {
        return new CountryComparisonResponse(buildProfile(idA), buildProfile(idB));
    }
}