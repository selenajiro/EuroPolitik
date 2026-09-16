ALTER TABLE country ADD COLUMN current_ruling_party VARCHAR(300);
ALTER TABLE country ADD COLUMN ruling_party_source_name VARCHAR(200);
ALTER TABLE country ADD COLUMN ruling_party_source_url VARCHAR(500);
ALTER TABLE country ADD COLUMN ruling_party_updated_at TIMESTAMP;