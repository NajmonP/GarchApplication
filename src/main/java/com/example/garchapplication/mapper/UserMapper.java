package com.example.garchapplication.mapper;

import com.example.garchapplication.model.dto.api.UserProfileDTO;
import com.example.garchapplication.model.entity.User;

public final class UserMapper {
    public static UserProfileDTO toUserProfileDTO(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
