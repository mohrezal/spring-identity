package com.github.mohrezal.identity.domain.authentication.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class AuthCannotRevokeCurrentSessionException extends BadRequestException {
    public AuthCannotRevokeCurrentSessionException() {
        super(AppMessage.AUTH_CANNOT_REVOKE_CURRENT_SESSION);
    }
}
