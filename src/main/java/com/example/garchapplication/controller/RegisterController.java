package com.example.garchapplication.controller;

import com.example.garchapplication.exception.InvalidCredentialsException;
import com.example.garchapplication.model.dto.api.RegisterRequest;
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
    public String register(@Valid @ModelAttribute("form") RegisterRequest form,
                           BindingResult bindingResult,
                           Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("form", form);
            model.addAttribute("modalError", "Neplatné údaje pro registraci");
            return "register";
        }

        try {
            registerService.register(form);
            return "redirect:/login";
        } catch (InvalidCredentialsException e) {
            model.addAttribute("form", form);
            model.addAttribute("modalError", e.getMessage());
            return "register";
        }
    }
}
