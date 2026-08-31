package io.github.selenajiro.europolitik.electionresult;

import io.github.selenajiro.europolitik.election.Election;
import io.github.selenajiro.europolitik.politicalparty.PoliticalParty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "election_result")
public class ElectionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", nullable = false)
    private Election election;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "party_id", nullable = false)
    private PoliticalParty party;

    private Long votes;

    @Column(precision = 7, scale = 4)
    private BigDecimal votePercentage;

    private Integer seats;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Election getElection() { return election; }
    public void setElection(Election election) { this.election = election; }

    public PoliticalParty getParty() { return party; }
    public void setParty(PoliticalParty party) { this.party = party; }

    public Long getVotes() { return votes; }
    public void setVotes(Long votes) { this.votes = votes; }

    public BigDecimal getVotePercentage() { return votePercentage; }
    public void setVotePercentage(BigDecimal votePercentage) { this.votePercentage = votePercentage; }

    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
