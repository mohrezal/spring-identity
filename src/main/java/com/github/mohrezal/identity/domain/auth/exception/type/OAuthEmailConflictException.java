package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class OAuthEmailConflictException extends ConflictException {
    public OAuthEmailConflictException() {
        super(AppMessage.OAUTH_EMAIL_CONFLICT);
    }
}
