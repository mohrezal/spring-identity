package com.github.mohrezal.identity.domain.auth.command.param;

import com.github.mohrezal.identity.domain.auth.dto.ChangePasswordRequest;
import com.github.mohrezal.identity.shared.interfaces.AuthenticatedParams;
import org.springframework.security.core.userdetails.UserDetails;

public record ChangePasswordCommandParams(UserDetails userDetails, ChangePasswordRequest request)
        implements AuthenticatedParams {
    @Override
    public UserDetails getUserDetails() {
        return userDetails;
    }
}
