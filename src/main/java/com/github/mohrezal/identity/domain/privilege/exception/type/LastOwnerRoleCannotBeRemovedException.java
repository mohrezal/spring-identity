package com.github.mohrezal.identity.domain.privilege.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class LastOwnerRoleCannotBeRemovedException extends ConflictException {

    public LastOwnerRoleCannotBeRemovedException() {
        super(AppMessage.PRIVILEGE_LAST_OWNER_ROLE_CANNOT_BE_REMOVED);
    }
}
