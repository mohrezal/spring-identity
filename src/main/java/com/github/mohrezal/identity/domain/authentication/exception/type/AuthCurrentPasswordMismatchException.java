package com.github.mohrezal.identity.domain.authentication.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class AuthCurrentPasswordMismatchException extends BadRequestException {
    public AuthCurrentPasswordMismatchException() {
        super(AppMessage.AUTH_CURRENT_PASSWORD_MISMATCH);
    }
}
