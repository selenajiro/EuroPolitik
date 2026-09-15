package io.github.selenajiro.europolitik.electionimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParlGovElectionRow {

    @JsonProperty("country_name_short")
    public String countryNameShort;

    @JsonProperty("election_type")
    public String electionType;

    @JsonProperty("election_date")
    public String electionDate;

    @JsonProperty("vote_share")
    public String voteShare;

    @JsonProperty("seats")
    public String seats;

    @JsonProperty("party_name_short")
    public String partyNameShort;

    @JsonProperty("party_name")
    public String partyName;

    @JsonProperty("party_name_english")
    public String partyNameEnglish;

    @JsonProperty("election_id")
    public String electionId;

    @JsonProperty("party_id")
    public String partyId;
}
