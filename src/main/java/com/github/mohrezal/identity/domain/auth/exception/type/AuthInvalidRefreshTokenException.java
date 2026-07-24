package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;

public class AuthInvalidRefreshTokenException extends UnauthorizedException {

    public AuthInvalidRefreshTokenException() {
        super(ExceptionCode.AUTH_INVALID_REFRESH_TOKEN);
    }
}
