CREATE TABLE country_statistic (
                                   id BIGSERIAL PRIMARY KEY,
                                   country_id BIGINT NOT NULL REFERENCES country(id),

                                   indicator VARCHAR(100) NOT NULL,
                                   year INT NOT NULL,
                                   value DECIMAL(20,6),
                                   unit VARCHAR(50),

                                   source_name VARCHAR(200),
                                   source_url VARCHAR(500),

                                   created_at TIMESTAMP NOT NULL,
                                   updated_at TIMESTAMP NOT NULL,

                                   CONSTRAINT uq_country_statistic_country_indicator_year UNIQUE (country_id, indicator, year)
);

CREATE INDEX idx_country_statistic_country_id ON country_statistic (country_id);
CREATE INDEX idx_country_statistic_indicator ON country_statistic (indicator);
CREATE INDEX idx_country_statistic_year ON country_statistic (year);