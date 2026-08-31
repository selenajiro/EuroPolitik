CREATE TABLE political_party (
                                 id BIGSERIAL PRIMARY KEY,
                                 country_id BIGINT NOT NULL REFERENCES country(id),

                                 name VARCHAR(200) NOT NULL,
                                 short_name VARCHAR(50),
                                 political_group VARCHAR(100),
                                 ideology VARCHAR(100),

                                 source_name VARCHAR(200),
                                 source_url VARCHAR(500),

                                 created_at TIMESTAMP NOT NULL,
                                 updated_at TIMESTAMP NOT NULL,

                                 CONSTRAINT uq_political_party_country_name UNIQUE (country_id, name)
);

CREATE INDEX idx_political_party_country_id ON political_party (country_id);