package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class AuthCurrentPasswordMismatchException extends BadRequestException {
    public AuthCurrentPasswordMismatchException() {
        super(ExceptionCode.AUTH_CURRENT_PASSWORD_MISMATCH);
    }
}
