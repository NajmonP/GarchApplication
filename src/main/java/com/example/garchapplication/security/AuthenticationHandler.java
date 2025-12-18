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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof String s) {
            if("anonymousUser".equals(s)){
                return null;
            }
        }

        UserDetails userDetails = (UserDetails) principal;
        Long userId = ((UserDetailsImpl) userDetails).getId();
        return userService.getUserById(userId);
    }
}
