package com.github.mohrezal.identity.domain.authentication.command.param;

import com.github.mohrezal.identity.shared.interfaces.AuthenticatedParams;
import org.springframework.security.core.userdetails.UserDetails;

public record LogoutAllCommandParams(UserDetails userDetails) implements AuthenticatedParams {
    @Override
    public UserDetails getUserDetails() {
        return userDetails;
    }
}
