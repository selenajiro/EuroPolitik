CREATE TABLE saved_comparison (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_id BIGINT NOT NULL REFERENCES user_account(id),

                                  country_a_id BIGINT NOT NULL REFERENCES country(id),
                                  country_b_id BIGINT NOT NULL REFERENCES country(id),

                                  created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_saved_comparison_user_id ON saved_comparison (user_id);
CREATE INDEX idx_saved_comparison_country_a_id ON saved_comparison (country_a_id);
CREATE INDEX idx_saved_comparison_country_b_id ON saved_comparison (country_b_id);