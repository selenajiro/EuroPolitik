package io.github.selenajiro.europolitik.mep;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mep")
public class Mep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(nullable = false, length = 200)
    private String fullName;

    @Column(length = 200)
    private String nationalParty;

    @Column(length = 100)
    private String politicalGroup;

    @Column(length = 50)
    private String parliamentaryTerm;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(length = 200)
    private String sourceName;

    @Column(length = 500)
    private String sourceUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNationalParty() { return nationalParty; }
    public void setNationalParty(String nationalParty) { this.nationalParty = nationalParty; }

    public String getPoliticalGroup() { return politicalGroup; }
    public void setPoliticalGroup(String politicalGroup) { this.politicalGroup = politicalGroup; }

    public String getParliamentaryTerm() { return parliamentaryTerm; }
    public void setParliamentaryTerm(String parliamentaryTerm) { this.parliamentaryTerm = parliamentaryTerm; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
