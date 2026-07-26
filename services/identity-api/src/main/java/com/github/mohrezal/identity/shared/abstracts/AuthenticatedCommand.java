package com.github.mohrezal.identity.shared.abstracts;

import com.github.mohrezal.identity.shared.interfaces.AuthenticatedParams;
import com.github.mohrezal.identity.shared.interfaces.Command;

public abstract class AuthenticatedCommand<P extends AuthenticatedParams, R>
        extends AuthenticatedBase<P> implements Command<P, R> {}
