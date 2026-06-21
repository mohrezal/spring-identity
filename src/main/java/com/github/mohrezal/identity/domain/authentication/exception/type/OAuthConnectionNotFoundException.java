package com.github.mohrezal.identity.domain.authentication.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;

public class OAuthConnectionNotFoundException extends NotFoundException {

    public OAuthConnectionNotFoundException() {
        super(AppMessage.OAUTH_CONNECTION_NOT_FOUND);
    }
}
