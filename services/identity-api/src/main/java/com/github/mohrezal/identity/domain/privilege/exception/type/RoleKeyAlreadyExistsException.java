package com.github.mohrezal.identity.domain.privilege.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class RoleKeyAlreadyExistsException extends ConflictException {

    public RoleKeyAlreadyExistsException() {
        super(ExceptionCode.PRIVILEGE_ROLE_KEY_ALREADY_EXISTS);
    }
}
