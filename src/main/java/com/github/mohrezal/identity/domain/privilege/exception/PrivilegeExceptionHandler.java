package com.github.mohrezal.identity.domain.privilege.exception;

import com.github.mohrezal.identity.domain.privilege.exception.type.ConfiguredRoleCannotBeDeletedException;
import com.github.mohrezal.identity.domain.privilege.exception.type.LastOwnerRoleCannotBeRemovedException;
import com.github.mohrezal.identity.domain.privilege.exception.type.OwnerRoleCannotBeUpdatedException;
import com.github.mohrezal.identity.domain.privilege.exception.type.PermissionNotFoundException;
import com.github.mohrezal.identity.domain.privilege.exception.type.RoleAssignedToUsersException;
import com.github.mohrezal.identity.domain.privilege.exception.type.RoleKeyAlreadyExistsException;
import com.github.mohrezal.identity.domain.privilege.exception.type.RoleNotFoundException;
import com.github.mohrezal.identity.shared.exception.AbstractExceptionHandler;
import com.github.mohrezal.identity.shared.exception.ErrorResponse;
import com.github.mohrezal.identity.shared.exception.type.BaseException;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PrivilegeExceptionHandler extends AbstractExceptionHandler {

    public PrivilegeExceptionHandler(MessageSource messageSource) {
        super(messageSource);
    }

    @ExceptionHandler({
        ConfiguredRoleCannotBeDeletedException.class,
        LastOwnerRoleCannotBeRemovedException.class,
        OwnerRoleCannotBeUpdatedException.class,
        PermissionNotFoundException.class,
        RoleAssignedToUsersException.class,
        RoleKeyAlreadyExistsException.class,
        RoleNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handlePrivilegeException(
            BaseException exception, WebRequest request) {
        return buildErrorResponse(exception, request);
    }
}
