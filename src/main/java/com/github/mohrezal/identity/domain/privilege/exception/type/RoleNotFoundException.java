package com.github.mohrezal.identity.domain.privilege.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;

public class RoleNotFoundException extends NotFoundException {

    public RoleNotFoundException() {
        super(ExceptionCode.PRIVILEGE_ROLE_NOT_FOUND);
    }
}
