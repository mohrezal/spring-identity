package com.github.mohrezal.identity.domain.user.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException() {
        super(AppMessage.USER_NOT_FOUND);
    }
}
