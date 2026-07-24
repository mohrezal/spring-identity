package com.github.mohrezal.identity.support.assertion;

import com.github.mohrezal.identity.shared.exception.type.BaseException;

public final class ErrorResponseAssertions {

    private ErrorResponseAssertions() {}

    public static String errorName(BaseException exception) {
        return exception.getExceptionCode().name();
    }
}
