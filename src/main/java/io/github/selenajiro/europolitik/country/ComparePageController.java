package io.github.selenajiro.europolitik.country;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ComparePageController {

    @GetMapping("/compare")
    public String compare() {
        return "compare";
    }
}
