UPDATE saved_comparison
SET country_a_id = country_b_id, country_b_id = country_a_id
WHERE country_a_id > country_b_id;

DELETE FROM saved_comparison sc
WHERE EXISTS (
    SELECT 1 FROM saved_comparison sc2
    WHERE sc2.user_id = sc.user_id
      AND sc2.country_a_id = sc.country_a_id
      AND sc2.country_b_id = sc.country_b_id
      AND sc2.id < sc.id
);

ALTER TABLE saved_comparison ADD CONSTRAINT uq_saved_comparison_user_countries UNIQUE (user_id, country_a_id, country_b_id);