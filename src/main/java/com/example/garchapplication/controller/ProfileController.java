package com.example.garchapplication.controller;

import com.example.garchapplication.model.dto.api.ChangePasswordRequest;
import com.example.garchapplication.model.dto.api.UpdateUserRequest;
import com.example.garchapplication.model.dto.api.UserProfileDTO;
import com.example.garchapplication.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfileController {

    private final UserService userService;

    @Autowired
    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/profile/data")
    @ResponseBody
    public UserProfileDTO profileData() {
        return userService.getUserProfile();
    }

    @PutMapping("/profile")
    @ResponseBody
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody UpdateUserRequest updateUserRequest) {
        userService.updateUser(updateUserRequest);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/profile/password")
    @ResponseBody
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/profile")
    @ResponseBody
    public ResponseEntity<Void> deleteProfile(HttpServletRequest request,
                                              HttpServletResponse response) {

        userService.deleteUser();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }

        return ResponseEntity.noContent().build();
    }
}
