package io.github.selenajiro.europolitik.savedcomparison;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SavedComparisonRepository extends JpaRepository<SavedComparison, Long> {

    @Query("SELECT s FROM SavedComparison s JOIN FETCH s.countryA JOIN FETCH s.countryB WHERE s.user.username = :username")
    List<SavedComparison> findAllByUsername(String username);

    Optional<SavedComparison> findByIdAndUserUsername(Long id, String username);
}
