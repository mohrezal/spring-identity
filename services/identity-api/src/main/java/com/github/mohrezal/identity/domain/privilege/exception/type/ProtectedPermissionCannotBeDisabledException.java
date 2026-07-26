package com.github.mohrezal.identity.domain.privilege.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class ProtectedPermissionCannotBeDisabledException extends ConflictException {

    public ProtectedPermissionCannotBeDisabledException() {
        super(ExceptionCode.PRIVILEGE_PROTECTED_PERMISSION_CANNOT_BE_DISABLED);
    }
}
