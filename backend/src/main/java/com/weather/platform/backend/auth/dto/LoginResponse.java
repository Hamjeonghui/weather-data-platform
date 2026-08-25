package com.weather.platform.backend.auth.dto;

public record LoginResponse(
        String accessToken,
        UserResponse user
) {
}
