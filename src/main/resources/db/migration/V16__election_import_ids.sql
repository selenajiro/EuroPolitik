ALTER TABLE political_party ADD COLUMN parlgov_party_id BIGINT;
ALTER TABLE political_party ADD CONSTRAINT uk_political_party_parlgov_id UNIQUE (parlgov_party_id);

ALTER TABLE election ADD COLUMN parlgov_election_id BIGINT;
ALTER TABLE election ADD CONSTRAINT uk_election_parlgov_id UNIQUE (parlgov_election_id);

ALTER TABLE election_result ADD CONSTRAINT uk_election_result_election_party UNIQUE (election_id, party_id);