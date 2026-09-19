package io.github.selenajiro.europolitik.rulingpartyimport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Response shape of MediaWiki's action=query&list=categorymembers. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CategoryMembersResponseDto(QueryResult query) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record QueryResult(List<CategoryMember> categorymembers) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CategoryMember(long pageid, int ns, String title) {}
}
