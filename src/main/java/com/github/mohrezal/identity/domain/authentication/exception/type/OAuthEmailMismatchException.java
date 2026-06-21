package com.github.mohrezal.identity.domain.authentication.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class OAuthEmailMismatchException extends ConflictException {
    public OAuthEmailMismatchException() {
        super(AppMessage.OAUTH_EMAIL_MISMATCH);
    }
}
