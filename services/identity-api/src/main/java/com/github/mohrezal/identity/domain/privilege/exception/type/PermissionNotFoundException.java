package com.github.mohrezal.identity.domain.privilege.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;

public class PermissionNotFoundException extends NotFoundException {

    public PermissionNotFoundException() {
        super(ExceptionCode.PRIVILEGE_PERMISSION_NOT_FOUND);
    }
}
