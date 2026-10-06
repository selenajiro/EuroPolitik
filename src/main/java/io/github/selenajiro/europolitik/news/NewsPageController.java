package io.github.selenajiro.europolitik.news;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NewsPageController {

    @GetMapping("/news")
    public String news() {
        return "news";
    }
}
