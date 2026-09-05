package io.github.selenajiro.europolitik.country;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private static final Map<String, List<String>> OTHER_REAL_WORLD_NEIGHBORS = Map.of(
            "TR", List.of("Georgia", "Armenia", "Azerbaijan", "Iran", "Iraq", "Syria"),
            "RU", List.of("Georgia", "Azerbaijan", "Kazakhstan", "Mongolia", "China", "North Korea")
    );

    private final CountryService countryService;
    private final CountryProfileService countryProfileService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeoJsonWriter geoJsonWriter = new GeoJsonWriter(4);

    public CountryController(CountryService countryService, CountryProfileService countryProfileService) {
        this.countryService = countryService;
        this.countryProfileService = countryProfileService;
        this.geoJsonWriter.setEncodeCRS(false);
    }

    @GetMapping
    public List<CountryResponse> findAll() {
        return countryService.findAll().stream()
                .map(CountryResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CountryResponse findById(@PathVariable Long id) {
        return CountryResponse.from(countryService.findById(id));
    }

    @GetMapping("/{id}/profile")
    public CountryProfileResponse profile(@PathVariable Long id) {
        return countryProfileService.buildProfile(id);
    }

    @GetMapping("/{id}/neighbors")
    public CountryNeighborsResponse neighbors(@PathVariable Long id) {
        Country country = countryService.findById(id);

        List<CountryNeighborsResponse.NeighborSummary> inDataset = countryService.findNeighbors(id).stream()
                .map(CountryNeighborsResponse.NeighborSummary::from)
                .toList();

        List<String> otherNeighbors = OTHER_REAL_WORLD_NEIGHBORS.getOrDefault(country.getIsoCode(), List.of());

        return new CountryNeighborsResponse(inDataset, otherNeighbors);
    }

    @GetMapping(value = "/geojson", produces = MediaType.APPLICATION_JSON_VALUE)
    public String geoJson() {
        List<Country> countries = countryService.findAll();

        ObjectNode featureCollection = objectMapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        ArrayNode features = featureCollection.putArray("features");

        for (Country country : countries) {
            ObjectNode feature = features.addObject();
            feature.put("type", "Feature");

            ObjectNode properties = feature.putObject("properties");
            properties.put("id", country.getId());
            properties.put("isoCode", country.getIsoCode());
            properties.put("name", country.getName());
            properties.put("euMember", country.isEuMember());
            properties.put("schengenMember", country.isSchengenMember());
            properties.put("eurozoneMember", country.isEurozoneMember());
            properties.put("natoMember", country.isNatoMember());

            try {
                JsonNode geometryNode = objectMapper.readTree(geoJsonWriter.write(country.getGeometry()));
                feature.set("geometry", geometryNode);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert geometry for " + country.getIsoCode(), e);
            }
        }

        try {
            return objectMapper.writeValueAsString(featureCollection);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize GeoJSON response", e);
        }
    }
}