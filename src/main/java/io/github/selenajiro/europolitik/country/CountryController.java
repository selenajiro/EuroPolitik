package io.github.selenajiro.europolitik.country;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private final Geometry europeClip;

    private volatile String cachedGeoJson;

    public CountryController(CountryService countryService, CountryProfileService countryProfileService) {
        this.countryService = countryService;
        this.countryProfileService = countryProfileService;
        this.geoJsonWriter.setEncodeCRS(false);

        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Coordinate[] clipCoords = {
                new Coordinate(-25, 34),
                new Coordinate(45, 34),
                new Coordinate(45, 71),
                new Coordinate(-25, 71),
                new Coordinate(-25, 34)
        };
        this.europeClip = geometryFactory.createPolygon(clipCoords);
    }

    @GetMapping
    public List<CountryResponse> findAll() {
        return countryService.findAllProjected();
    }

    @GetMapping("/{id}")
    public CountryResponse findById(@PathVariable Long id) {
        return countryService.findByIdProjected(id);
    }

    @GetMapping("/{id}/profile")
    public CountryProfileResponse profile(@PathVariable Long id) {
        return countryProfileService.buildProfile(id);
    }

    @GetMapping("/compare")
    public CountryComparisonResponse compare(@RequestParam Long a, @RequestParam Long b) {
        return countryProfileService.compare(a, b);
    }

    @GetMapping("/{id}/neighbors")
    public CountryNeighborsResponse neighbors(@PathVariable Long id) {
        CountryResponse country = countryService.findByIdProjected(id);

        List<CountryNeighborsResponse.NeighborSummary> inDataset = countryService.findNeighbors(id).stream()
                .map(CountryNeighborsResponse.NeighborSummary::from)
                .toList();

        List<String> otherNeighbors = OTHER_REAL_WORLD_NEIGHBORS.getOrDefault(country.isoCode(), List.of());

        return new CountryNeighborsResponse(inDataset, otherNeighbors);
    }

    @GetMapping("/{id}/nearby")
    public List<CountryNeighborsResponse.NeighborSummary> nearby(@PathVariable Long id) {
        return countryService.findClosestCountries(id).stream()
                .map(CountryNeighborsResponse.NeighborSummary::from)
                .toList();
    }

    @GetMapping(value = "/geojson", produces = MediaType.APPLICATION_JSON_VALUE)
    public String geoJson() {
        if (cachedGeoJson != null) {
            return cachedGeoJson;
        }

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

            Point centroid = country.getGeometry().getCentroid();
            properties.put("centroidLat", centroid.getY());
            properties.put("centroidLng", centroid.getX());

            try {
                Geometry displayGeometry = country.getGeometry().buffer(0).intersection(europeClip);
                JsonNode geometryNode = objectMapper.readTree(geoJsonWriter.write(displayGeometry));
                feature.set("geometry", geometryNode);
            } catch (Exception e) {
                throw new RuntimeException("Failed to convert geometry for " + country.getIsoCode(), e);
            }
        }

        try {
            cachedGeoJson = objectMapper.writeValueAsString(featureCollection);
            return cachedGeoJson;
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize GeoJSON response", e);
        }
    }
}