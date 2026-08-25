package com.weather.platform.backend.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.weather.platform.backend.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private static final String TEST_SECRET = "test-secret-key-for-jwt-provider-unit-test-1234567890";

    @Test
    void 토큰을_생성하고_파싱하면_원래_값을_복원한다() {
        JwtProvider jwtProvider = new JwtProvider(TEST_SECRET, 60_000L);

        String token = jwtProvider.createAccessToken("admin", Role.ADMIN);
        Claims claims = jwtProvider.parseClaims(token);

        assertThat(jwtProvider.getLoginId(claims)).isEqualTo("admin");
        assertThat(jwtProvider.getRole(claims)).isEqualTo(Role.ADMIN);
    }

    @Test
    void 만료된_토큰을_파싱하면_예외가_발생한다() {
        JwtProvider jwtProvider = new JwtProvider(TEST_SECRET, -1_000L);

        String expiredToken = jwtProvider.createAccessToken("admin", Role.ADMIN);

        assertThatThrownBy(() -> jwtProvider.parseClaims(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
