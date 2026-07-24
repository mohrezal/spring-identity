package com.github.mohrezal.identity.shared.abstracts;

import com.github.mohrezal.identity.shared.interfaces.AuthenticatedParams;
import com.github.mohrezal.identity.shared.interfaces.Query;

public abstract class AuthenticatedQuery<P extends AuthenticatedParams, R>
        extends AuthenticatedBase<P> implements Query<P, R> {}
