package io.github.selenajiro.europolitik.politicalevent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PoliticalEventRepository extends JpaRepository<PoliticalEvent, Long> {

    @Query("SELECT e FROM PoliticalEvent e LEFT JOIN FETCH e.country")
    List<PoliticalEvent> findAllWithCountry();

    @Query("SELECT e FROM PoliticalEvent e LEFT JOIN FETCH e.country WHERE e.id = :id")
    Optional<PoliticalEvent> findByIdWithCountry(Long id);
}
