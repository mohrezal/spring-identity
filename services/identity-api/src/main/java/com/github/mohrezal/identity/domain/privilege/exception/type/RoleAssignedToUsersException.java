package com.github.mohrezal.identity.domain.privilege.exception.type;

import com.github.mohrezal.identity.shared.enums.ExceptionCode;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class RoleAssignedToUsersException extends ConflictException {

    public RoleAssignedToUsersException() {
        super(ExceptionCode.PRIVILEGE_ROLE_ASSIGNED_TO_USERS);
    }
}
