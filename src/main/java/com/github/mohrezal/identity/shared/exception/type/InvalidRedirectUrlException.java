package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;

public class InvalidRedirectUrlException extends BadRequestException {

    public InvalidRedirectUrlException() {
        super(ErrorCode.INVALID_REDIRECT_URL);
    }
}
