CREATE TABLE import_run (
                            id BIGSERIAL PRIMARY KEY,

                            source VARCHAR(100) NOT NULL,
                            started_at TIMESTAMP NOT NULL,
                            finished_at TIMESTAMP,

                            status VARCHAR(30) NOT NULL,

                            records_read INT NOT NULL DEFAULT 0,
                            records_inserted INT NOT NULL DEFAULT 0,
                            records_updated INT NOT NULL DEFAULT 0,
                            records_skipped INT NOT NULL DEFAULT 0,

                            error_message TEXT,

                            created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_import_run_source ON import_run (source);
CREATE INDEX idx_import_run_started_at ON import_run (started_at);
CREATE INDEX idx_import_run_status ON import_run (status);
