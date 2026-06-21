package com.github.mohrezal.identity.domain.authentication.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ForbiddenException;

public class AuthEmailNotVerifiedException extends ForbiddenException {

    public AuthEmailNotVerifiedException() {
        super(AppMessage.AUTH_EMAIL_NOT_VERIFIED);
    }
}
