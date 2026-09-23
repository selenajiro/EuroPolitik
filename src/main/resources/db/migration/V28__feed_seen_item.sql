CREATE TABLE feed_seen_item (
                                id BIGSERIAL PRIMARY KEY,
                                user_id BIGINT NOT NULL REFERENCES user_account(id),
                                item_key VARCHAR(50) NOT NULL,
                                first_shown_at TIMESTAMP NOT NULL,
                                CONSTRAINT uk_feed_seen_item_user_key UNIQUE (user_id, item_key)
);

CREATE INDEX idx_feed_seen_item_user ON feed_seen_item(user_id);