package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;

public class AuthInvalidCredentialsException extends UnauthorizedException {

    public AuthInvalidCredentialsException() {
        super(ErrorCode.AUTH_INVALID_CREDENTIALS);
    }
}
