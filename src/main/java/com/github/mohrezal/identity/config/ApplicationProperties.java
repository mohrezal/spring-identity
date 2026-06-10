package com.github.mohrezal.identity.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(@Valid Security security) {

    @Validated
    public record Security(
            @NotBlank String secret,
            @NotNull Duration verificationTokenTtl,
            @NotNull Duration passwordResetTokenTtl,
            @NotNull @Size(min = 1) List<String> allowedOrigins,
            @Valid Cookie cookie) {}

    @Validated
    public record Cookie(
            @Valid Csrf csrf, @Valid TokenCookie accessToken, @Valid TokenCookie refreshToken) {

        @Validated
        public record Csrf(
                @NotBlank String path, @NotNull Boolean secure, @NotBlank String sameSite) {}

        @Validated
        public record TokenCookie(
                @NotBlank String name,
                @NotBlank String path,
                @NotNull Boolean httpOnly,
                @NotNull Boolean secure,
                @NotBlank String sameSite,
                @NotNull Duration ttl) {

            public ResponseCookie build(String value) {
                return ResponseCookie.from(name, value)
                        .path(path)
                        .httpOnly(httpOnly)
                        .secure(secure)
                        .sameSite(sameSite)
                        .maxAge(ttl)
                        .build();
            }

            public ResponseCookie build(String value, String path) {
                return ResponseCookie.from(name, value)
                        .path(path)
                        .httpOnly(httpOnly)
                        .secure(secure)
                        .sameSite(sameSite)
                        .maxAge(ttl)
                        .build();
            }

            public ResponseCookie clear() {
                return ResponseCookie.from(name, "")
                        .path(path)
                        .httpOnly(httpOnly)
                        .secure(secure)
                        .sameSite(sameSite)
                        .maxAge(0)
                        .build();
            }
        }
    }
}
