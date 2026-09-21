ALTER TABLE country
    ADD COLUMN current_parliament_leaning NUMERIC(3, 1),
    ADD COLUMN current_parliament_leaning_label VARCHAR(20),
    ADD COLUMN parliament_leaning_computed_at TIMESTAMP;