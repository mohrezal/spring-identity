package com.github.mohrezal.identity.domain.user.exception.type;

import com.github.mohrezal.identity.shared.exception.ErrorCode;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class UserInvalidVerificationTokenException extends BadRequestException {

    public UserInvalidVerificationTokenException() {
        super(ErrorCode.USER_INVALID_VERIFICATION_TOKEN);
    }
}
