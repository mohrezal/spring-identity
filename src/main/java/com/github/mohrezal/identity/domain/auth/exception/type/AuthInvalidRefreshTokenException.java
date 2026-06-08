package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;

public class AuthInvalidRefreshTokenException extends UnauthorizedException {

    public AuthInvalidRefreshTokenException() {
        super(ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }
}
