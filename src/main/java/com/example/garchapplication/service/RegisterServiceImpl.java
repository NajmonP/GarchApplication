package com.example.garchapplication.service;

import com.example.garchapplication.exception.InvalidRegisterException;
import com.example.garchapplication.model.dto.api.RegisterRequest;
import com.example.garchapplication.model.entity.User;
import com.example.garchapplication.model.enums.RoleType;
import com.example.garchapplication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegisterServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest registerRequest) {
        if (!registerRequest.password().equals(registerRequest.confirmPassword())) {
            throw new InvalidRegisterException("Hesla se neshodují.");
        }
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new InvalidRegisterException("Uživatelské jméno již existuje.");
        }
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new InvalidRegisterException("Účet s emailovou adresou již existuje.");
        }

        User user = new User();
        user.setUsername(registerRequest.username());
        user.setEmail(registerRequest.email());
        user.setHashedPassword(passwordEncoder.encode(registerRequest.password()));
        user.setRole(RoleType.USER);
        user.setCreated(Instant.now());
        userRepository.save(user);
    }
}
