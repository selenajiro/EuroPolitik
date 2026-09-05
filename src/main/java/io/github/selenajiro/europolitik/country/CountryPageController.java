package io.github.selenajiro.europolitik.country;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CountryPageController {

    @GetMapping("/countries/{id}")
    public String countryDetail(@PathVariable Long id, Model model) {
        model.addAttribute("countryId", id);
        return "country-detail";
    }
}
