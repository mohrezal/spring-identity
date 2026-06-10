package com.github.mohrezal.identity.domain.auth.command;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.auth.command.param.RefreshTokenCommandParams;
import com.github.mohrezal.identity.domain.auth.dto.AuthResponse;
import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import com.github.mohrezal.identity.shared.interfaces.Command;
import com.github.mohrezal.identity.shared.service.HashService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenCommand implements Command<RefreshTokenCommandParams, AuthResponse> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final HashService hashService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationProperties applicationProperties;

    @Override
    public void validate(RefreshTokenCommandParams params) {
        if (params.rawRefreshToken() == null) {
            throw new UnauthorizedException();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AuthResponse execute(RefreshTokenCommandParams params) {
        System.out.println(params.rawRefreshToken());
        var hashedRefreshToken = hashService.sha256(params.rawRefreshToken());
        var refreshToken =
                refreshTokenRepository
                        .findByHashedToken(hashedRefreshToken)
                        .orElseThrow(UnauthorizedException::new);
        if (!refreshToken.isActive()) {
            throw new UnauthorizedException();
        }

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
        var user = refreshToken.getUser();
        var newAccessToken = jwtTokenProvider.createAccessToken(user.getId());
        var newRawRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        var newRefreshToken =
                RefreshToken.builder()
                        .deviceInfo(params.userAgent())
                        .ipAddress(params.ipAddress())
                        .user(user)
                        .hashedToken(hashService.sha256(newRawRefreshToken))
                        .expiresAt(
                                OffsetDateTime.now()
                                        .plus(
                                                applicationProperties
                                                        .security()
                                                        .cookie()
                                                        .refreshToken()
                                                        .ttl()))
                        .build();

        refreshTokenRepository.save(newRefreshToken);

        return new AuthResponse(newAccessToken, newRawRefreshToken);
    }
}
