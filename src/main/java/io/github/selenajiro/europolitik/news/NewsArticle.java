package io.github.selenajiro.europolitik.news;

import io.github.selenajiro.europolitik.country.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "news_article")
@Getter
@Setter
@NoArgsConstructor
public class NewsArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "source_url", nullable = false, length = 1000, unique = true)
    private String sourceUrl;

    @Column(name = "source_domain", length = 200)
    private String sourceDomain;

    @Column(name = "source_pipeline", nullable = false, length = 50)
    private String sourcePipeline;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    @Column(length = 10)
    private String language;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
