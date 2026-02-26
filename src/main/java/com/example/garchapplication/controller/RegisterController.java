package com.example.garchapplication.controller;

import com.example.garchapplication.model.dto.RegisterRequest;
import com.example.garchapplication.service.RegisterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    private final RegisterService registerService;

    @Autowired
    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("form", new RegisterRequest("", "", "", ""));
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@ModelAttribute("form") @Valid RegisterRequest form, BindingResult br) {
        if (br.hasErrors()) {
            return "register";
        }

        registerService.register(form);
        return "redirect:/login?registered";
    }
}
