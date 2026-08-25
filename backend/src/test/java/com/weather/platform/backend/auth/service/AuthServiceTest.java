package com.weather.platform.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.weather.platform.backend.auth.dto.LoginRequest;
import com.weather.platform.backend.auth.dto.UserResponse;
import com.weather.platform.backend.global.exception.BusinessException;
import com.weather.platform.backend.global.exception.ErrorCode;
import com.weather.platform.backend.global.security.JwtProvider;
import com.weather.platform.backend.user.entity.Role;
import com.weather.platform.backend.user.entity.User;
import com.weather.platform.backend.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private User activeUser() {
        return new User("admin", "encoded-password", Role.ADMIN, true, OffsetDateTime.now(), null);
    }

    @Test
    void 로그인에_성공하면_액세스_토큰과_사용자_정보를_반환한다() {
        given(userRepository.findById("admin")).willReturn(Optional.of(activeUser()));
        given(passwordEncoder.matches("password", "encoded-password")).willReturn(true);
        given(jwtProvider.createAccessToken("admin", Role.ADMIN)).willReturn("access-token");

        var response = authService.login(new LoginRequest("admin", "password"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.user()).isEqualTo(new UserResponse("admin", Role.ADMIN));
    }

    @Test
    void 존재하지_않는_아이디로_로그인하면_INVALID_CREDENTIALS_예외가_발생한다() {
        given(userRepository.findById("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("unknown", "password")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        verify(jwtProvider, never()).createAccessToken(any(), any());
    }

    @Test
    void 비밀번호가_일치하지_않으면_INVALID_CREDENTIALS_예외가_발생한다() {
        given(userRepository.findById("admin")).willReturn(Optional.of(activeUser()));
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "wrong-password")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void 비활성화된_사용자는_비밀번호가_맞아도_INVALID_CREDENTIALS_예외가_발생한다() {
        User disabledUser = new User("admin", "encoded-password", Role.ADMIN, false, OffsetDateTime.now(), null);
        given(userRepository.findById("admin")).willReturn(Optional.of(disabledUser));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "password")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void 토큰의_사용자가_존재하면_내정보를_반환한다() {
        given(userRepository.findById("admin")).willReturn(Optional.of(activeUser()));

        UserResponse response = authService.getMe("admin");

        assertThat(response).isEqualTo(new UserResponse("admin", Role.ADMIN));
    }

    @Test
    void 토큰의_사용자가_존재하지_않으면_USER_NOT_FOUND_예외가_발생한다() {
        given(userRepository.findById("ghost")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getMe("ghost"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
