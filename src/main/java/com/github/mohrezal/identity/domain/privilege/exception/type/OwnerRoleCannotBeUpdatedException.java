package com.github.mohrezal.identity.domain.privilege.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class OwnerRoleCannotBeUpdatedException extends ConflictException {

    public OwnerRoleCannotBeUpdatedException() {
        super(AppMessage.PRIVILEGE_OWNER_ROLE_CANNOT_BE_UPDATED);
    }
}
