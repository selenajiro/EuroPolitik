package io.github.selenajiro.europolitik.country;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CountryRepository extends JpaRepository<Country, Long> {

    @Query(value = """
        SELECT c2.* FROM country c1
        JOIN country c2 ON ST_Intersects(c1.geometry, c2.geometry) AND c1.id <> c2.id
        WHERE c1.id = :countryId
        ORDER BY c2.name
        """, nativeQuery = true)
    List<Country> findNeighbors(Long countryId);
}
