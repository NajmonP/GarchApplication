package com.example.garchapplication.service;

import com.example.garchapplication.model.dto.api.ChangePasswordRequest;
import com.example.garchapplication.model.dto.api.UpdateUserRequest;
import com.example.garchapplication.model.dto.api.UserProfileDTO;
import com.example.garchapplication.model.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public interface UserService extends UserDetailsService {
    List<User> getAllUsers();

    void saveUser(User user);

    @Transactional
    void updateUser(UpdateUserRequest updateUserRequest);

    @Transactional
    void changePassword(ChangePasswordRequest changePasswordRequest);

    @Transactional
    void deleteUser();

    @Transactional
    User getUserById(Long id);

    UserProfileDTO getUserProfile();

}
