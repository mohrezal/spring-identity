package com.github.mohrezal.identity.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(
        @NotNull @Valid Security security,
        @NotNull @Valid RateLimit rateLimit,
        @NotNull @Valid Seed seed) {

    @Validated
    public record Security(
            @NotBlank String secret,
            @NotNull Duration verificationTokenTtl,
            @NotNull Duration passwordResetTokenTtl,
            @NotNull @Size(min = 1) List<String> allowedOrigins,
            @Valid Cookie cookie,
            @Valid OAuth oAuth) {}

    @Validated
    public record Cookie(
            @Valid Csrf csrf,
            @Valid TokenCookie accessToken,
            @Valid TokenCookie refreshToken,
            @Valid TokenCookie oauthState) {

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

            public String valueFrom(jakarta.servlet.http.Cookie[] cookies) {
                if (cookies == null || cookies.length == 0) {
                    return null;
                }

                return Arrays.stream(cookies)
                        .filter(cookie -> name.equals(cookie.getName()))
                        .map(jakarta.servlet.http.Cookie::getValue)
                        .findFirst()
                        .orElse(null);
            }

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

            public ResponseCookie clear(String path) {
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

    @Validated
    public record OAuth(@Valid Google google) {
        @Validated
        public record Google(
                @NotBlank String clientId,
                @NotBlank String clientSecret,
                @NotBlank String redirectUri,
                @NotNull @Size(min = 1) List<String> scopes) {}
    }

    @Validated
    public record RateLimit(@NotEmpty List<@Valid Policy> policies) {

        @Validated
        public record Policy(
                @NotNull HttpMethod method,
                @NotBlank String path,
                @NotBlank String key,
                @NotNull Duration window,
                @Positive Integer ipLimit,
                @Positive Integer userLimit) {}
    }

    @Validated
    public record Seed(@NotNull @Valid Owner owner, @NotNull @Valid User user) {

        @Validated
        public record Owner(
                @NotBlank String email, @NotBlank String roleKey, @NotBlank String roleName) {}

        @Validated
        public record User(
                @NotBlank String roleKey,
                @NotBlank String roleName,
                @NotEmpty List<@NotBlank String> permissions) {}
    }
}
