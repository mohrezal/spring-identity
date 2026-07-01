package com.github.mohrezal.identity.domain.privilege.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class ProtectedPermissionCannotBeDisabledException extends ConflictException {

    public ProtectedPermissionCannotBeDisabledException() {
        super(AppMessage.PRIVILEGE_PROTECTED_PERMISSION_CANNOT_BE_DISABLED);
    }
}
