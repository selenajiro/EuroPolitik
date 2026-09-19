ALTER TABLE political_event ADD COLUMN source_article_title VARCHAR(300);
ALTER TABLE political_event ADD CONSTRAINT uk_political_event_source_article UNIQUE (source_article_title);