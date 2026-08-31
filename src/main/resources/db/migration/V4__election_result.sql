CREATE TABLE election_result (
                                 id BIGSERIAL PRIMARY KEY,
                                 election_id BIGINT NOT NULL REFERENCES election(id),
                                 party_id BIGINT NOT NULL REFERENCES political_party(id),

                                 votes BIGINT,
                                 vote_percentage DECIMAL(7,4),
                                 seats INT,

                                 created_at TIMESTAMP NOT NULL,
                                 updated_at TIMESTAMP NOT NULL,

                                 CONSTRAINT uq_election_result_election_party UNIQUE (election_id, party_id)
);

CREATE INDEX idx_election_result_election_id ON election_result (election_id);
CREATE INDEX idx_election_result_party_id ON election_result (party_id);