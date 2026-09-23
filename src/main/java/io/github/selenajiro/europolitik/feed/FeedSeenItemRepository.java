package io.github.selenajiro.europolitik.feed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedSeenItemRepository extends JpaRepository<FeedSeenItem, Long> {

    @Query("SELECT s FROM FeedSeenItem s WHERE s.user.id = :userId")
    List<FeedSeenItem> findAllByUserId(@Param("userId") Long userId);
}