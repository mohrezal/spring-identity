package com.github.mohrezal.identity.shared.abstracts;

import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import com.github.mohrezal.identity.shared.interfaces.AuthenticatedParams;

public abstract class AuthenticatedBase<P extends AuthenticatedParams> {

    protected User getCurrentUser(P params) {
        var userDetails = params.getUserDetails();
        if (!(userDetails instanceof User user)) {
            throw new UnauthorizedException();
        }

        return user;
    }
}
