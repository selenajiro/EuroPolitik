CREATE TABLE mep (
                     id BIGSERIAL PRIMARY KEY,
                     country_id BIGINT NOT NULL REFERENCES country(id),

                     full_name VARCHAR(200) NOT NULL,
                     national_party VARCHAR(200),
                     political_group VARCHAR(100),
                     parliamentary_term VARCHAR(50),

                     start_date DATE,
                     end_date DATE,

                     source_name VARCHAR(200),
                     source_url VARCHAR(500),

                     created_at TIMESTAMP NOT NULL,
                     updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_mep_country_id ON mep (country_id);
CREATE INDEX idx_mep_parliamentary_term ON mep (parliamentary_term);