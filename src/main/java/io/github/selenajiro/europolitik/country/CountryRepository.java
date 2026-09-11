package io.github.selenajiro.europolitik.country;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query(value = """
        SELECT c2.* FROM country c1
        JOIN country c2 ON ST_Intersects(c1.geometry, c2.geometry) AND c1.id <> c2.id
        WHERE c1.id = :countryId
        ORDER BY c2.name
        """, nativeQuery = true)
    List<Country> findNeighbors(Long countryId);

    @Query(value = """
        SELECT c2.* FROM country c1, country c2
        WHERE c1.id = :countryId AND c2.id <> :countryId
        ORDER BY c1.geometry <-> c2.geometry
        LIMIT 5
        """, nativeQuery = true)
    List<Country> findClosestCountries(Long countryId);

    @Query(value = "SELECT ST_Area(geometry::geography) / 1000000.0 FROM country WHERE id = :id", nativeQuery = true)
    Double findAreaSquareKm(Long id);

    @Query("SELECT new io.github.selenajiro.europolitik.country.CountryResponse(" +
            "c.id, c.isoCode, c.name, c.euMember, c.schengenMember, c.eurozoneMember, c.natoMember, c.createdAt, c.updatedAt) " +
            "FROM Country c")
    List<CountryResponse> findAllProjected();

    @Query("SELECT new io.github.selenajiro.europolitik.country.CountryResponse(" +
            "c.id, c.isoCode, c.name, c.euMember, c.schengenMember, c.eurozoneMember, c.natoMember, c.createdAt, c.updatedAt) " +
            "FROM Country c WHERE c.id = :id")
    Optional<CountryResponse> findByIdProjected(Long id);
}
