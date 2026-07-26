package com.github.mohrezal.identity.domain.auth.command;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.auth.command.param.UnlinkOAuthConnectionCommandParams;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthCannotUnlinkLastLoginMethodException;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthConnectionNotFoundException;
import com.github.mohrezal.identity.domain.auth.repository.UserOauthConnectionRepository;
import com.github.mohrezal.identity.domain.user.repository.UserCredentialRepository;
import com.github.mohrezal.identity.shared.abstracts.AuthenticatedCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnlinkOAuthConnectionCommand
        extends AuthenticatedCommand<UnlinkOAuthConnectionCommandParams, Void> {

    private final UserOauthConnectionRepository userOauthConnectionRepository;
    private final UserCredentialRepository userCredentialRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Void execute(
            UnlinkOAuthConnectionCommandParams params, AuditRequestContext auditRequestContext) {
        var user = getCurrentUser(params);
        var connection =
                userOauthConnectionRepository
                        .findByIdAndUser(params.connectionId(), user)
                        .orElseThrow(OAuthConnectionNotFoundException::new);

        var hasPasswordLogin = userCredentialRepository.existsByUser(user);
        var oauthConnectionCount = userOauthConnectionRepository.countByUser(user);

        if (!hasPasswordLogin && oauthConnectionCount <= 1) {
            throw new OAuthCannotUnlinkLastLoginMethodException();
        }

        userOauthConnectionRepository.delete(connection);
        log.info(
                "OAuth connection unlinked. userId={}, connectionId={}, provider={}",
                user.getId(),
                connection.getId(),
                connection.getProvider());

        return null;
    }
}
