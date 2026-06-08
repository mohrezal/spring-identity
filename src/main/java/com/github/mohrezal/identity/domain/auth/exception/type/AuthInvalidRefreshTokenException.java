package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;

public class AuthInvalidRefreshTokenException extends UnauthorizedException {

    public AuthInvalidRefreshTokenException() {
        super(AppMessage.AUTH_INVALID_REFRESH_TOKEN);
    }
}
