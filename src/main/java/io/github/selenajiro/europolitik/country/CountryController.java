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

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private final CountryRepository countryRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeoJsonWriter geoJsonWriter = new GeoJsonWriter(4);

    public CountryController(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
        this.geoJsonWriter.setEncodeCRS(false);
    }

    @GetMapping
    public List<Country> findAll() {
        return countryRepository.findAll();
    }

    @GetMapping("/{id}")
    public Country findById(@PathVariable Long id) {
        return countryRepository.findById(id).orElseThrow();
    }

    @GetMapping(value = "/geojson", produces = MediaType.APPLICATION_JSON_VALUE)
    public String geoJson() {
        List<Country> countries = countryRepository.findAll();

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