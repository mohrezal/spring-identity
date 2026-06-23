package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class OAuthProviderAlreadyLinkedException extends ConflictException {
    public OAuthProviderAlreadyLinkedException() {
        super(AppMessage.OAUTH_PROVIDER_ALREADY_LINKED);
    }
}
