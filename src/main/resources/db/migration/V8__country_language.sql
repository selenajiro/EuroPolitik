CREATE TABLE country_language (
                                  id BIGSERIAL PRIMARY KEY,
                                  country_id BIGINT NOT NULL REFERENCES country(id),

                                  language VARCHAR(100) NOT NULL,
                                  official BOOLEAN NOT NULL DEFAULT FALSE,
                                  minority BOOLEAN NOT NULL DEFAULT FALSE,
                                  language_family VARCHAR(100),

                                  source_name VARCHAR(200),
                                  source_url VARCHAR(500),

                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP NOT NULL,

                                  CONSTRAINT uq_country_language_country_language UNIQUE (country_id, language)
);

CREATE INDEX idx_country_language_country_id ON country_language (country_id);