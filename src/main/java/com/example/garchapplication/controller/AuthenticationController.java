package com.example.garchapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class AuthenticationController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
