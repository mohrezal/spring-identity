package com.github.mohrezal.identity.domain.auth.command;

import com.github.mohrezal.identity.domain.auth.command.param.LoginCommandParams;
import com.github.mohrezal.identity.domain.auth.dto.LoginResponse;
import com.github.mohrezal.identity.domain.auth.exception.type.AuthInvalidCredentialsException;
import com.github.mohrezal.identity.domain.auth.service.TokenIssuanceService;
import com.github.mohrezal.identity.domain.user.mapper.UserMapper;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.interfaces.Command;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginCommand implements Command<LoginCommandParams, LoginResponse> {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuanceService tokenIssuanceService;
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
            log.warn("Email/password login failed. userId={}", user.getId());
            throw new AuthInvalidCredentialsException();
        }

        log.info("Email/password login succeeded. userId={}", user.getId());

        var authResponse = tokenIssuanceService.issue(user, params.ipAddress(), params.userAgent());
        var userSummary = userMapper.toUserSummary(user);
        return new LoginResponse(authResponse, userSummary);
    }
}
