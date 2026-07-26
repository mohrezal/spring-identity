package com.github.mohrezal.identity.support;

import com.github.mohrezal.identity.domain.auth.service.oauth.OAuthProvider;
import com.github.mohrezal.identity.support.oauth.FakeOAuthProvider;
import org.springframework.test.context.bean.override.convention.TestBean;

public abstract class OAuthIntegrationTestSupport extends IntegrationTestSupport {

    @TestBean(name = "googleOAuthProvider", enforceOverride = true)
    private OAuthProvider oAuthProvider;

    private static OAuthProvider oAuthProvider() {
        return new FakeOAuthProvider();
    }
}
