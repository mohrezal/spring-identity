package com.github.mohrezal.identity.shared.service;

import static com.github.mohrezal.identity.support.data.TestConstants.Origin.CLIENT;
import static com.github.mohrezal.identity.support.data.TestConstants.Origin.LOCAL;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.mohrezal.identity.config.ApplicationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@ContextConfiguration(
        classes = RedirectValidationServiceTest.TestConfiguration.class,
        initializers = ConfigDataApplicationContextInitializer.class)
class RedirectValidationServiceTest {

    @Autowired
    private RedirectValidationService service;

    @ParameterizedTest
    @ValueSource(
            strings = {
                CLIENT,
                CLIENT + "/account/verified?source=email#result",
                LOCAL + "/oauth/callback"
            })
    void isValid_whenOriginIsExplicitlyAllowed_acceptsUrl(String redirectUrl) {
        assertThat(service.isValid(redirectUrl)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                "   ",
                "/relative/callback",
                "client.test/callback",
                "http://client.test/callback",
                CLIENT + ":443/callback",
                CLIENT + ".evil.test/callback",
                "https://client.test@evil.test/callback",
                "https://[invalid"
            })
    void isValid_whenOriginIsNotExactlyAllowed_rejectsUrl(String redirectUrl) {
        assertThat(service.isValid(redirectUrl)).isFalse();
    }

    @Test
    void isValid_whenMultipleOriginsAreConfigured_acceptsEachExactOrigin() {
        assertThat(service.isValid(CLIENT + "/complete")).isTrue();
        assertThat(service.isValid(LOCAL + "/complete")).isTrue();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ApplicationProperties.class)
    static class TestConfiguration {

        @Bean
        RedirectValidationService redirectValidationService(
                ApplicationProperties applicationProperties) {
            return new RedirectValidationService(applicationProperties);
        }
    }
}
