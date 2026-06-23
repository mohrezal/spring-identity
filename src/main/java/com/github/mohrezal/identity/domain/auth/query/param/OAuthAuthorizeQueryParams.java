package com.github.mohrezal.identity.domain.auth.query.param;

import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.shared.interfaces.AuthenticatedParams;
import org.springframework.security.core.userdetails.UserDetails;

public record OAuthAuthorizeQueryParams(
        OAuthProviderType providerType,
        OAuthFlowType flowType,
        String redirectUrl,
        UserDetails userDetails)
        implements AuthenticatedParams {
    @Override
    public UserDetails getUserDetails() {
        return userDetails;
    }
}
