package com.weather.platform.backend.auth.dto;

import com.weather.platform.backend.user.entity.Role;
import com.weather.platform.backend.user.entity.User;

public record UserResponse(
        String loginId,
        Role role
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getLoginId(), user.getRole());
    }
}
