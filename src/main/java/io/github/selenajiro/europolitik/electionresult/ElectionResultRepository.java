package io.github.selenajiro.europolitik.electionresult;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ElectionResultRepository extends JpaRepository<ElectionResult, Long> {

    @Query("SELECT r FROM ElectionResult r JOIN FETCH r.election JOIN FETCH r.party")
    List<ElectionResult> findAllWithDetails();

    @Query("SELECT r FROM ElectionResult r JOIN FETCH r.election JOIN FETCH r.party WHERE r.id = :id")
    Optional<ElectionResult> findByIdWithDetails(Long id);
}
