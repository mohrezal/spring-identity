package com.github.mohrezal.identity.domain.user.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class UserAlreadyVerifiedException extends BadRequestException {

    public UserAlreadyVerifiedException() {
        super(ErrorCode.USER_ALREADY_VERIFIED);
    }
}
