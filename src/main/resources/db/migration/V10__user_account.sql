CREATE TABLE user_account (
                              id BIGSERIAL PRIMARY KEY,

                              username VARCHAR(50) NOT NULL,
                              password_hash VARCHAR(255) NOT NULL,
                              role VARCHAR(30) NOT NULL DEFAULT 'USER',

                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP NOT NULL,

                              CONSTRAINT uq_user_account_username UNIQUE (username)
);