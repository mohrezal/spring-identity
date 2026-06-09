package com.github.mohrezal.identity.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(@Valid Security security) {

    @Validated
    public record Security(
            @NotBlank String secret,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration refreshTokenTtl,
            @NotNull Duration verificationTokenTtl,
            @NotNull Duration passwordResetTokenTtl,
            @NotNull String allowedOrigin,
            @Valid Csrf csrf) {}

    @Validated
    public record Csrf(@Valid Cookie cookie) {

        @Validated
        public record Cookie(
                @NotBlank String path, @NotNull Boolean secure, @NotBlank String sameSite) {}
    }
}
