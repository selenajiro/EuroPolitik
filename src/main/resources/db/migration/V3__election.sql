CREATE TABLE election (
                          id BIGSERIAL PRIMARY KEY,
                          country_id BIGINT NOT NULL REFERENCES country(id),

                          name VARCHAR(200) NOT NULL,
                          type VARCHAR(50) NOT NULL,
                          election_date DATE NOT NULL,

                          source_name VARCHAR(200),
                          source_url VARCHAR(500),
                          imported_at TIMESTAMP,

                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL,

                          CONSTRAINT uq_election_country_date_type UNIQUE (country_id, election_date, type)
);

CREATE INDEX idx_election_country_id ON election (country_id);
CREATE INDEX idx_election_election_date ON election (election_date);