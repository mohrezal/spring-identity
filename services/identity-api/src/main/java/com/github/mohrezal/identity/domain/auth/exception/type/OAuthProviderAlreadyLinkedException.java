package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class OAuthProviderAlreadyLinkedException extends ConflictException {
    public OAuthProviderAlreadyLinkedException() {
        super(ExceptionCode.OAUTH_PROVIDER_ALREADY_LINKED);
    }
}
