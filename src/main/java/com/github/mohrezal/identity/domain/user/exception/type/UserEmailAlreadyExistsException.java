package com.github.mohrezal.identity.domain.user.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class UserEmailAlreadyExistsException extends ConflictException {

    public UserEmailAlreadyExistsException() {
        super(AppMessage.USER_EMAIL_ALREADY_EXISTS);
    }
}
