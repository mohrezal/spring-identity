package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;

public class AuthRefreshTokenNotFoundException extends NotFoundException {

    public AuthRefreshTokenNotFoundException() {
        super(ExceptionCode.AUTH_REFRESH_TOKEN_NOT_FOUND);
    }
}
