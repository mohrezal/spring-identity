package com.github.mohrezal.identity.shared.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.domain.auth.dto.ForgotPasswordRequest;
import com.github.mohrezal.identity.domain.auth.dto.LoginRequest;
import com.github.mohrezal.identity.domain.auth.dto.ResendEmailVerificationRequest;
import com.github.mohrezal.identity.domain.auth.dto.oauth.OAuthUserProfile;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.user.dto.RegisterRequest;
import org.junit.jupiter.api.Test;

class EmailAddressNormalizerTest {

    private static final String RAW_EMAIL = " User@Example.COM ";
    private static final String CANONICAL_EMAIL = "user@example.com";

    @Test
    void normalize_trimsAndLowercasesUsingOneRepresentation() {
        assertThat(EmailAddressNormalizer.normalize(RAW_EMAIL)).isEqualTo(CANONICAL_EMAIL);
        assertThat(EmailAddressNormalizer.normalize(null)).isNull();
    }

    @Test
    void emailIngressRecords_storeCanonicalValues() {
        var register = new RegisterRequest("Test", "User", RAW_EMAIL, "Password123!");
        var login = new LoginRequest(RAW_EMAIL, "Password123!");
        var forgotPassword = new ForgotPasswordRequest(RAW_EMAIL);
        var resendVerification = new ResendEmailVerificationRequest(RAW_EMAIL);
        var oAuthProfile =
                new OAuthUserProfile(
                        "provider-user", RAW_EMAIL, true, "Test", "User", OAuthProviderType.GOOGLE);
        var owner = new ApplicationProperties.Owner(RAW_EMAIL);

        assertThat(register.email()).isEqualTo(CANONICAL_EMAIL);
        assertThat(login.email()).isEqualTo(CANONICAL_EMAIL);
        assertThat(forgotPassword.email()).isEqualTo(CANONICAL_EMAIL);
        assertThat(resendVerification.email()).isEqualTo(CANONICAL_EMAIL);
        assertThat(oAuthProfile.email()).isEqualTo(CANONICAL_EMAIL);
        assertThat(owner.email()).isEqualTo(CANONICAL_EMAIL);
    }
}
