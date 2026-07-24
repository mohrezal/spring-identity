package com.github.mohrezal.identity.shared.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;

public class InvalidRedirectUrlException extends BadRequestException {

    public InvalidRedirectUrlException() {
        super(ExceptionCode.INVALID_REDIRECT_URL);
    }
}
