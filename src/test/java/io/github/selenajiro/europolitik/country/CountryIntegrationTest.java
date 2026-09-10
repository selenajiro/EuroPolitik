package io.github.selenajiro.europolitik.country;

import io.github.selenajiro.europolitik.AbstractIntegrationTest;
import io.github.selenajiro.europolitik.politicalparty.PoliticalParty;
import io.github.selenajiro.europolitik.politicalparty.PoliticalPartyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CountryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private PoliticalPartyRepository politicalPartyRepository;

    private Country countryA;
    private Country countryB;

    @BeforeEach
    void setUp() throws Exception {
        WKTReader reader = new WKTReader();

        countryA = new Country();
        countryA.setIsoCode("XA");
        countryA.setName("Testland A");
        countryA.setEuMember(true);
        countryA.setSchengenMember(true);
        countryA.setEurozoneMember(true);
        countryA.setNatoMember(true);
        countryA.setGeometry((MultiPolygon) reader.read("MULTIPOLYGON(((0 0, 0 2, 2 2, 2 0, 0 0)))"));
        countryA.setCreatedAt(LocalDateTime.now());
        countryA.setUpdatedAt(LocalDateTime.now());
        countryA = countryRepository.save(countryA);

        countryB = new Country();
        countryB.setIsoCode("XB");
        countryB.setName("Testland B");
        countryB.setEuMember(false);
        countryB.setSchengenMember(false);
        countryB.setEurozoneMember(false);
        countryB.setNatoMember(false);
        // Shares the edge x=2 with countryA -- should register as a neighbor.
        countryB.setGeometry((MultiPolygon) reader.read("MULTIPOLYGON(((2 0, 2 2, 4 2, 4 0, 2 0)))"));
        countryB.setCreatedAt(LocalDateTime.now());
        countryB.setUpdatedAt(LocalDateTime.now());
        countryB = countryRepository.save(countryB);
    }

    @AfterEach
    void tearDown() {
        politicalPartyRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    void neighborsQueryFindsCountriesThatShareABorder() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/countries/" + countryA.getId() + "/neighbors", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Testland B");
    }

    @Test
    void requestingNonExistentCountryReturnsCleanNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/countries/999999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("\"status\":404");
    }

    @Test
    void politicalPartyJoinFetchAvoidsLazyInitializationException() {
        PoliticalParty party = new PoliticalParty();
        party.setCountry(countryA);
        party.setName("Test Party");
        party.setCreatedAt(LocalDateTime.now());
        party.setUpdatedAt(LocalDateTime.now());
        politicalPartyRepository.save(party);

        var parties = politicalPartyRepository.findAllWithCountry();

        assertThat(parties)
                .anySatisfy(p -> assertThat(p.getCountry().getName()).isEqualTo("Testland A"));
    }
}