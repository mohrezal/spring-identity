package com.github.mohrezal.identity.domain.user.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class UserAlreadyVerifiedException extends BadRequestException {

    public UserAlreadyVerifiedException() {
        super(ExceptionCode.USER_ALREADY_VERIFIED);
    }
}
