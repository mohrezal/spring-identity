package com.github.mohrezal.identity.domain.authentication.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;

public class AuthEmailAlreadyVerifiedException extends UnauthorizedException {
    public AuthEmailAlreadyVerifiedException() {
        super(AppMessage.AUTH_EMAIL_ALREADY_VERIFIED);
    }
}
