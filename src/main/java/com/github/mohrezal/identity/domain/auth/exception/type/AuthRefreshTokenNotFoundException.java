package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;

public class AuthRefreshTokenNotFoundException extends NotFoundException {

    public AuthRefreshTokenNotFoundException() {
        super(AppMessage.AUTH_REFRESH_TOKEN_NOT_FOUND);
    }
}
