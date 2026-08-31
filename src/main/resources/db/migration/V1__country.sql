CREATE TABLE country (
                         id BIGSERIAL PRIMARY KEY,
                         iso_code VARCHAR(2) NOT NULL,
                         name VARCHAR(100) NOT NULL,

                         eu_member BOOLEAN NOT NULL DEFAULT FALSE,
                         schengen_member BOOLEAN NOT NULL DEFAULT FALSE,
                         eurozone_member BOOLEAN NOT NULL DEFAULT FALSE,
                         nato_member BOOLEAN NOT NULL DEFAULT FALSE,

                         geometry geometry(MultiPolygon, 4326) NOT NULL,

                         created_at TIMESTAMP NOT NULL,
                         updated_at TIMESTAMP NOT NULL,

                         CONSTRAINT uq_country_iso_code UNIQUE (iso_code),
                         CONSTRAINT uq_country_name UNIQUE (name)
);

CREATE INDEX idx_country_geometry ON country USING GIST (geometry);