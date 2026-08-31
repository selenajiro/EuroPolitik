package io.github.selenajiro.europolitik.country;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.locationtech.jts.geom.MultiPolygon;

import java.time.LocalDateTime;

@Entity
@Table(name = "country")
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2)
    private String isoCode;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean euMember;

    @Column(nullable = false)
    private boolean schengenMember;

    @Column(nullable = false)
    private boolean eurozoneMember;

    @Column(nullable = false)
    private boolean natoMember;

    @JsonIgnore
    @Column(nullable = false)
    private MultiPolygon geometry;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIsoCode() { return isoCode; }
    public void setIsoCode(String isoCode) { this.isoCode = isoCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isEuMember() { return euMember; }
    public void setEuMember(boolean euMember) { this.euMember = euMember; }

    public boolean isSchengenMember() { return schengenMember; }
    public void setSchengenMember(boolean schengenMember) { this.schengenMember = schengenMember; }

    public boolean isEurozoneMember() { return eurozoneMember; }
    public void setEurozoneMember(boolean eurozoneMember) { this.eurozoneMember = eurozoneMember; }

    public boolean isNatoMember() { return natoMember; }
    public void setNatoMember(boolean natoMember) { this.natoMember = natoMember; }

    public MultiPolygon getGeometry() { return geometry; }
    public void setGeometry(MultiPolygon geometry) { this.geometry = geometry; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}