package com.example.garchapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
class AuthenticationController {

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("modalError", "Neplatné uživatelské jméno nebo heslo.");
        }

        return "login";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "403";
    }
}
