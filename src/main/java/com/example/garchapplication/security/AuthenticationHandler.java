package com.example.garchapplication.security;

import com.example.garchapplication.model.entity.User;
import com.example.garchapplication.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provides information about the currently authenticated user.
 */
@Component
public class AuthenticationHandler {

    private final UserService userService;

    @Autowired
    public AuthenticationHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns the current Authentication object or empty if not authenticated.
     *
     * @return optional of current authentication
     */
    public Optional<Authentication> getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(authentication);
    }

    /**
     * Returns User entity of authenticated user.
     *
     * @return User entity
     */
    public User getUserEntity(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = ((UserDetailsImpl) userDetails).getId();
        return userService.getUserById(userId);
    }

    public User getUserEntity(Authentication authentication){
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Long userId = ((UserDetailsImpl) userDetails).getId();
        return userService.getUserById(userId);
    }
}
