package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class OAuthEmailMismatchException extends ConflictException {
    public OAuthEmailMismatchException() {
        super(ExceptionCode.OAUTH_EMAIL_MISMATCH);
    }
}
