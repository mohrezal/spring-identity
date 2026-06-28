package com.github.mohrezal.identity.domain.privilege.exception.type;

import com.github.mohrezal.identity.shared.enums.AppMessage;
import com.github.mohrezal.identity.shared.exception.type.ConflictException;

public class ConfiguredRoleCannotBeDeletedException extends ConflictException {

    public ConfiguredRoleCannotBeDeletedException() {
        super(AppMessage.PRIVILEGE_CONFIGURED_ROLE_CANNOT_BE_DELETED);
    }
}
