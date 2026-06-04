package com.github.mohrezal.identity.shared.interfaces;

interface Operation<P, R> {
    default void validate(P params) {}

    R execute(P params);
}
