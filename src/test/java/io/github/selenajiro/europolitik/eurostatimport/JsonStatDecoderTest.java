package io.github.selenajiro.europolitik.eurostatimport;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class JsonStatDecoderTest {

    @Test
    void decodesTwoDimensionsInRowMajorOrder() {
        // 2 countries x 2 years, "time" is the faster-varying (last)
        // dimension, matching how Eurostat actually encodes it.
        JsonStatDto dto = new JsonStatDto(
                List.of("geo", "time"),
                List.of(2, 2),
                Map.of(
                        "geo", new JsonStatDimensionDto("Country", new JsonStatCategoryDto(Map.of("DE", 0, "FR", 1), Map.of())),
                        "time", new JsonStatDimensionDto("Time", new JsonStatCategoryDto(Map.of("2023", 0, "2024", 1), Map.of()))
                ),
                Map.of(
                        "0", new BigDecimal("100"),
                        "1", new BigDecimal("101"),
                        "2", new BigDecimal("200"),
                        "3", new BigDecimal("201")
                )
        );

        List<JsonStatDecoder.Observation> result = JsonStatDecoder.decode(dto);

        assertThat(result).extracting(o -> o.code("geo"), o -> o.code("time"), JsonStatDecoder.Observation::value)
                .containsExactlyInAnyOrder(
                        tuple("DE", "2023", new BigDecimal("100")),
                        tuple("DE", "2024", new BigDecimal("101")),
                        tuple("FR", "2023", new BigDecimal("200")),
                        tuple("FR", "2024", new BigDecimal("201"))
                );
    }
}
