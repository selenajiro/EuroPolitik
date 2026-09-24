package io.github.selenajiro.europolitik.news;

public class GdeltThrottledException extends RuntimeException {
    public GdeltThrottledException(String preview) {
        super("GDELT returned non-JSON, likely throttled: " + preview);
    }
}
