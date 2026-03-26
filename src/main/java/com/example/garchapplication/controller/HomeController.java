package com.example.garchapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/")
class HomeController {

    @GetMapping("/")
    String home() {
        return "index";
    }
}
