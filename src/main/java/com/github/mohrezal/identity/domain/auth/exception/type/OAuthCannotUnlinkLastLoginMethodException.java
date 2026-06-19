package com.github.mohrezal.identity.domain.auth.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class OAuthCannotUnlinkLastLoginMethodException extends BadRequestException {

    public OAuthCannotUnlinkLastLoginMethodException() {
        super(AppMessage.OAUTH_CANNOT_UNLINK_LAST_LOGIN_METHOD);
    }
}
