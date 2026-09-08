package io.github.selenajiro.europolitik.politicalevent;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TimelinePageController {

    @GetMapping("/timeline")
    public String timeline() {
        return "timeline";
    }
}