ALTER TABLE political_event ADD COLUMN IF NOT EXISTS source_election_id BIGINT;
ALTER TABLE political_event ADD CONSTRAINT uk_political_event_source_election UNIQUE (source_election_id);