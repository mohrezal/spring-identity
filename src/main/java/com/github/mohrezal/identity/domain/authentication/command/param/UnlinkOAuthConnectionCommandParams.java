package com.github.mohrezal.identity.domain.authentication.command.param;

import com.github.mohrezal.identity.shared.interfaces.AuthenticatedParams;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;

public record UnlinkOAuthConnectionCommandParams(UserDetails userDetails, UUID connectionId)
        implements AuthenticatedParams {

    @Override
    public UserDetails getUserDetails() {
        return userDetails;
    }
}
