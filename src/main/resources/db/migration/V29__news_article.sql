CREATE TABLE news_article (
                              id BIGSERIAL PRIMARY KEY,
                              country_id BIGINT REFERENCES country(id),
                              title VARCHAR(500) NOT NULL,
                              source_url VARCHAR(1000) NOT NULL,
                              source_domain VARCHAR(200),
                              source_pipeline VARCHAR(50) NOT NULL,
                              published_date DATE NOT NULL,
                              language VARCHAR(10),
                              image_url VARCHAR(1000),
                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP NOT NULL,
                              CONSTRAINT uk_news_article_source_url UNIQUE (source_url)
);

CREATE INDEX idx_news_article_country ON news_article(country_id);
CREATE INDEX idx_news_article_published_date ON news_article(published_date);