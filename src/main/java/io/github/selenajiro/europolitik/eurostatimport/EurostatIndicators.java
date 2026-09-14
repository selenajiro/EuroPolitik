package io.github.selenajiro.europolitik.eurostatimport;

import java.util.Map;

public class EurostatIndicators {

    public static final EurostatIndicatorSpec POPULATION = new EurostatIndicatorSpec(
            "POPULATION",
            "demo_gind",
            Map.of("indic_de", "JAN", "sinceTimePeriod", "2011"),
            "persons",
            "https://ec.europa.eu/eurostat/databrowser/view/demo_gind/default/table"
    );

    public static final EurostatIndicatorSpec UNEMPLOYMENT_RATE = new EurostatIndicatorSpec(
            "UNEMPLOYMENT_RATE",
            "une_rt_a",
            Map.of("age", "Y15-74", "unit", "PC_ACT", "sex", "T", "lastTimePeriod", "15"),
            "%",
            "https://ec.europa.eu/eurostat/databrowser/view/une_rt_a/default/table"
    );

    private EurostatIndicators() {
    }
}
