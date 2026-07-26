package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class OAuthEmailConflictException extends ConflictException {
    public OAuthEmailConflictException() {
        super(ExceptionCode.OAUTH_EMAIL_CONFLICT);
    }
}
