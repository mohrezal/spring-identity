package com.github.mohrezal.identity.domain.privilege.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class RoleKeyAlreadyExistsException extends ConflictException {

    public RoleKeyAlreadyExistsException() {
        super(AppMessage.PRIVILEGE_ROLE_KEY_ALREADY_EXISTS);
    }
}
