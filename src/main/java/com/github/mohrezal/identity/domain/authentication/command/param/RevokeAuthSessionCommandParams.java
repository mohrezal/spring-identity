package com.github.mohrezal.identity.domain.authentication.command.param;

import com.github.mohrezal.identity.shared.interfaces.AuthenticatedParams;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;

public record RevokeAuthSessionCommandParams(
        UserDetails userDetails, UUID sessionId, String rawRefreshToken)
        implements AuthenticatedParams {
    @Override
    public UserDetails getUserDetails() {
        return userDetails;
    }
}
