package com.github.mohrezal.identity.domain.authentication.command;

import com.github.mohrezal.identity.domain.authentication.command.param.RevokeAuthSessionCommandParams;
import com.github.mohrezal.identity.domain.authentication.exception.type.AuthCannotRevokeCurrentSessionException;
import com.github.mohrezal.identity.domain.authentication.exception.type.AuthSessionNotFoundException;
import com.github.mohrezal.identity.domain.authentication.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.shared.abstracts.AuthenticatedCommand;
import com.github.mohrezal.identity.shared.service.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevokeAuthSessionCommand
        extends AuthenticatedCommand<RevokeAuthSessionCommandParams, Void> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final HashService hashService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void execute(RevokeAuthSessionCommandParams params) {
        var user = getCurrentUser(params);
        var targetSession =
                refreshTokenRepository
                        .findByIdAndUser(params.sessionId(), user)
                        .orElseThrow(AuthSessionNotFoundException::new);

        if (StringUtils.hasText(params.rawRefreshToken())) {
            var hashedRefreshToken = hashService.hashHex(params.rawRefreshToken());

            if (hashedRefreshToken.equals(targetSession.getHashedToken())) {
                throw new AuthCannotRevokeCurrentSessionException();
            }
        }

        if (targetSession.isActive()) {
            targetSession.revoke();
            refreshTokenRepository.save(targetSession);
        }

        log.info(
                "Auth session revoked. userId={}, sessionId={}",
                user.getId(),
                targetSession.getId());

        return null;
    }
}
