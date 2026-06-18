package com.github.mohrezal.identity.domain.auth.query.param;

import com.github.mohrezal.identity.shared.interfaces.AuthenticatedParams;
import org.springframework.security.core.userdetails.UserDetails;

public record GetAuthSessionsQueryParams(UserDetails userDetails) implements AuthenticatedParams {
    @Override
    public UserDetails getUserDetails() {
        return userDetails;
    }
}
