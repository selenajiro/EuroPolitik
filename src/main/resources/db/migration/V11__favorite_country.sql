CREATE TABLE favorite_country (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_id BIGINT NOT NULL REFERENCES user_account(id),
                                  country_id BIGINT NOT NULL REFERENCES country(id),

                                  created_at TIMESTAMP NOT NULL,

                                  CONSTRAINT uq_favorite_country_user_country UNIQUE (user_id, country_id)
);

CREATE INDEX idx_favorite_country_user_id ON favorite_country (user_id);
CREATE INDEX idx_favorite_country_country_id ON favorite_country (country_id);