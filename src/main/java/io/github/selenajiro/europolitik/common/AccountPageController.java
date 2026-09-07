package io.github.selenajiro.europolitik.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountPageController {

    @GetMapping("/account")
    public String account() {
        return "account";
    }
}
