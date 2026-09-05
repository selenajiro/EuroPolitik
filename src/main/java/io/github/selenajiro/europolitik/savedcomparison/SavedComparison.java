package io.github.selenajiro.europolitik.savedcomparison;

import io.github.selenajiro.europolitik.country.Country;
import io.github.selenajiro.europolitik.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "saved_comparison")
public class SavedComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_a_id", nullable = false)
    private Country countryA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_b_id", nullable = false)
    private Country countryB;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }

    public Country getCountryA() { return countryA; }
    public void setCountryA(Country countryA) { this.countryA = countryA; }

    public Country getCountryB() { return countryB; }
    public void setCountryB(Country countryB) { this.countryB = countryB; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}