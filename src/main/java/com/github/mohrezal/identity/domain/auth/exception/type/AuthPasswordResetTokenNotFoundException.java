package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class AuthPasswordResetTokenNotFoundException extends BadRequestException {
    public AuthPasswordResetTokenNotFoundException() {
        super(ExceptionCode.AUTH_PASSWORD_RESET_TOKEN_NOT_FOUND);
    }
}
