package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;

public class InvalidRedirectUrlException extends BadRequestException {

    public InvalidRedirectUrlException() {
        super(AppMessage.INVALID_REDIRECT_URL);
    }
}
