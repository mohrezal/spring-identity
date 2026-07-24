package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class AuthCannotRevokeCurrentSessionException extends BadRequestException {
    public AuthCannotRevokeCurrentSessionException() {
        super(ExceptionCode.AUTH_CANNOT_REVOKE_CURRENT_SESSION);
    }
}
