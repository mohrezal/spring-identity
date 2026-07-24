package com.github.mohrezal.identity.domain.user.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class UserInvalidVerificationTokenException extends BadRequestException {

    public UserInvalidVerificationTokenException() {
        super(ExceptionCode.USER_INVALID_VERIFICATION_TOKEN);
    }
}
