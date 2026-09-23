package io.github.selenajiro.europolitik.news;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    Optional<NewsArticle> findBySourceUrl(String sourceUrl);

    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.country WHERE n.country.id = :countryId ORDER BY n.publishedDate DESC")
    List<NewsArticle> findAllByCountryId(@Param("countryId") Long countryId);

    @Query("SELECT n FROM NewsArticle n LEFT JOIN FETCH n.country ORDER BY n.publishedDate DESC")
    List<NewsArticle> findAllWithCountry();

    @Query("SELECT n FROM NewsArticle n WHERE n.country IS NULL ORDER BY n.publishedDate DESC")
    List<NewsArticle> findAllWithNoCountry();
}
