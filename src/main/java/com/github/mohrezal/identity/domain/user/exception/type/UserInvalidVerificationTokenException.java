package com.github.mohrezal.identity.domain.user.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.BadRequestException;

public class UserInvalidVerificationTokenException extends BadRequestException {

    public UserInvalidVerificationTokenException() {
        super(AppMessage.USER_INVALID_VERIFICATION_TOKEN);
    }
}
