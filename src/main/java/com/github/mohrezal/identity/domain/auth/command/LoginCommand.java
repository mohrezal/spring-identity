package com.github.mohrezal.identity.domain.auth.command;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.auth.command.param.LoginCommandParams;
import com.github.mohrezal.identity.domain.auth.dto.AuthResponse;
import com.github.mohrezal.identity.domain.auth.dto.LoginResponse;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthInvalidCredentialsException;
import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.user.mapper.UserMapper;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.interfaces.Command;
import com.github.mohrezal.identity.shared.service.HashService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginCommand implements Command<LoginCommandParams, LoginResponse> {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final HashService hashService;
    private final ApplicationProperties applicationProperties;
    private final UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LoginResponse execute(LoginCommandParams params) {
        var user =
                userRepository
                        .findByEmail(params.request().email())
                        .orElseThrow(AuthInvalidCredentialsException::new);
        var userCredential =
                userCredentialRepository
                        .findByUser(user)
                        .orElseThrow(AuthInvalidCredentialsException::new);
        var isValidPassword =
                passwordEncoder.matches(
                        params.request().password(), userCredential.getHashedPassword());
        if (!isValidPassword) {
            throw new AuthInvalidCredentialsException();
        }

        var issuedAccessToken = jwtTokenProvider.createAccessToken(user.getId());
        var issuedRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        var refreshToken =
                RefreshToken.builder()
                        .deviceInfo(params.userAgent())
                        .ipAddress(params.ipAddress())
                        .hashedToken(hashService.sha256(issuedRefreshToken))
                        .user(user)
                        .expiresAt(
                                OffsetDateTime.now()
                                        .plus(
                                                applicationProperties
                                                        .security()
                                                        .cookie()
                                                        .refreshToken()
                                                        .ttl()))
                        .build();

        refreshTokenRepository.save(refreshToken);

        var authResponse = new AuthResponse(issuedAccessToken, issuedRefreshToken);
        var userSummary = userMapper.toUserSummary(user);
        return new LoginResponse(authResponse, userSummary);
    }
}
