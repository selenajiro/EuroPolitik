package io.github.selenajiro.europolitik.news;

/** GDELT rejected the query itself (e.g. "too short or too long") -- a query-construction problem, not throttling. Retrying the identical query would fail identically every time, so this is handled as a single, non-retryable failure per country rather than burning through backoff attempts for nothing. */
public class GdeltBadQueryException extends RuntimeException {
    public GdeltBadQueryException(String message) {
        super(message);
    }
}
