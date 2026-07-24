package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;

public class AuthSessionNotFoundException extends NotFoundException {
    public AuthSessionNotFoundException() {
        super(ExceptionCode.AUTH_SESSION_NOT_FOUND);
    }
}
