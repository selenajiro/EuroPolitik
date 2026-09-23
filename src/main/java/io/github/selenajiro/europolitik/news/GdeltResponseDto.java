package io.github.selenajiro.europolitik.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GdeltResponseDto(List<GdeltArticleDto> articles) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GdeltArticleDto(
            String url,
            String title,
            String seendate,
            String domain,
            String language,
            String sourcecountry,
            String socialimage
    ) {}
}
