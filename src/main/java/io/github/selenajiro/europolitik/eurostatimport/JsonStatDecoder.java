package io.github.selenajiro.europolitik.eurostatimport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonStatDecoder {

    public record Observation(Map<String, String> dimensionCodes, BigDecimal value) {
        public String code(String dimensionName) {
            return dimensionCodes.get(dimensionName);
        }
    }

    private JsonStatDecoder() {
    }

    public static List<Observation> decode(JsonStatDto dto) {
        List<Observation> observations = new ArrayList<>();
        if (dto == null || dto.value() == null || dto.id() == null || dto.size() == null) {
            return observations;
        }

        List<String> dimensionIds = dto.id();
        List<Integer> sizes = dto.size();

        Map<String, Map<Integer, String>> positionToCode = new HashMap<>();
        for (String dimensionId : dimensionIds) {
            JsonStatDimensionDto dimension = dto.dimension() == null ? null : dto.dimension().get(dimensionId);
            Map<Integer, String> reverse = new HashMap<>();
            if (dimension != null && dimension.category() != null && dimension.category().index() != null) {
                dimension.category().index().forEach((code, position) -> reverse.put(position, code));
            }
            positionToCode.put(dimensionId, reverse);
        }

        for (Map.Entry<String, BigDecimal> entry : dto.value().entrySet()) {
            long flatIndex;
            try {
                flatIndex = Long.parseLong(entry.getKey());
            } catch (NumberFormatException e) {
                continue;
            }

            Map<String, String> dimensionCodes = new HashMap<>();
            long remaining = flatIndex;
            for (int i = dimensionIds.size() - 1; i >= 0; i--) {
                int size = sizes.get(i);
                int position = size == 0 ? 0 : (int) (remaining % size);
                remaining = size == 0 ? remaining : remaining / size;
                dimensionCodes.put(dimensionIds.get(i), positionToCode.get(dimensionIds.get(i)).get(position));
            }

            observations.add(new Observation(dimensionCodes, entry.getValue()));
        }

        return observations;
    }
}
