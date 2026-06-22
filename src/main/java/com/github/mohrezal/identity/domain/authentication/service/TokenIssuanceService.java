package com.github.mohrezal.identity.domain.authentication.service;

import com.github.mohrezal.identity.config.security.JwtTokenProvider;
import com.github.mohrezal.identity.domain.authentication.dto.AuthResponse;
import com.github.mohrezal.identity.domain.authentication.model.RefreshToken;
import com.github.mohrezal.identity.domain.authentication.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.domain.authorization.service.UserPermissionService;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import com.github.mohrezal.identity.shared.service.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenIssuanceService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final HashService hashService;
    private final UserPermissionService userPermissionService;

    public AuthResponse issue(User user, String ipAddress, String deviceInfo) {
        var permissions = userPermissionService.getPermissionKeys(user.getId());
        var accessToken = jwtTokenProvider.createAccessToken(user.getId(), permissions);
        var refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        var entity =
                RefreshToken.builder()
                        .user(user)
                        .hashedToken(hashService.hashHex(refreshToken))
                        .ipAddress(ipAddress)
                        .deviceInfo(deviceInfo)
                        .expiresAt(
                                jwtTokenProvider
                                        .extractExpiration(refreshToken)
                                        .orElseThrow(UnauthorizedException::new))
                        .build();

        var savedRefreshToken = refreshTokenRepository.save(entity);

        log.info(
                "Issued auth tokens. userId={}, refreshTokenId={}",
                user.getId(),
                savedRefreshToken.getId());

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse rotate(RefreshToken refreshToken, String ipAddress, String deviceInfo) {
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        log.info(
                "Revoked refresh token during rotation. userId={}, refreshTokenId={}",
                refreshToken.getUser().getId(),
                refreshToken.getId());

        return issue(refreshToken.getUser(), ipAddress, deviceInfo);
    }
}
