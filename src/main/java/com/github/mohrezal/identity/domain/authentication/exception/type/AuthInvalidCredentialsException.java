package com.github.mohrezal.identity.domain.authentication.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;

public class AuthInvalidCredentialsException extends UnauthorizedException {

    public AuthInvalidCredentialsException() {
        super(AppMessage.AUTH_INVALID_CREDENTIALS);
    }
}
