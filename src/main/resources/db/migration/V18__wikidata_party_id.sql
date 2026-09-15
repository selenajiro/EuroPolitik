ALTER TABLE political_party ADD COLUMN wikidata_party_id VARCHAR(20);
ALTER TABLE political_party ADD CONSTRAINT uk_political_party_wikidata_id UNIQUE (wikidata_party_id);