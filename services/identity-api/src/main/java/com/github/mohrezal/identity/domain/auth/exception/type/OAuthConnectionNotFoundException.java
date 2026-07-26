package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;

public class OAuthConnectionNotFoundException extends NotFoundException {

    public OAuthConnectionNotFoundException() {
        super(ExceptionCode.OAUTH_CONNECTION_NOT_FOUND);
    }
}
