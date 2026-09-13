package io.github.selenajiro.europolitik.mepimport;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EpMepListResponseDto(
        @JsonAlias({"data", "@graph"})
        List<EpMepDto> data
) {
    public List<EpMepDto> membersOrEmpty() {
        return data == null ? List.of() : data;
    }
}
