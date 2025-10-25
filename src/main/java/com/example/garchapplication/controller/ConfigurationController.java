package com.example.garchapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class ConfigurationController {

    @GetMapping("/configuration")
    public String configuration() {
        return "configuration";
    }
}
