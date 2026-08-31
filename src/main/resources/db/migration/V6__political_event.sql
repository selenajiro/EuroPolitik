CREATE TABLE political_event (
                                 id BIGSERIAL PRIMARY KEY,
                                 country_id BIGINT REFERENCES country(id),

                                 event_type VARCHAR(50) NOT NULL,
                                 title VARCHAR(300) NOT NULL,
                                 description TEXT,

                                 event_date DATE NOT NULL,

                                 source_name VARCHAR(200),
                                 source_url VARCHAR(500),

                                 created_at TIMESTAMP NOT NULL,
                                 updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_political_event_country_id ON political_event (country_id);
CREATE INDEX idx_political_event_event_date ON political_event (event_date);
CREATE INDEX idx_political_event_event_type ON political_event (event_type);