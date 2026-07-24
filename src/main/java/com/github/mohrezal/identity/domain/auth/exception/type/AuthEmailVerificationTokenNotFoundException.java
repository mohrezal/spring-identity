package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;

public class AuthEmailVerificationTokenNotFoundException extends NotFoundException {

    public AuthEmailVerificationTokenNotFoundException() {
        super(ExceptionCode.AUTH_EMAIL_VERIFICATION_NOT_FOUND);
    }
}
