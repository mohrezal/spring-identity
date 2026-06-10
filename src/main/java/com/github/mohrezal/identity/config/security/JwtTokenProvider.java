package com.github.mohrezal.identity.config.security;

import com.github.mohrezal.identity.config.ApplicationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenProvider {

    private static final long ALLOWED_CLOCK_SKEW_SECONDS = 30L;

    private final JwtParser jwtParser;

    private final SecretKey signingKey;

    private final ApplicationProperties applicationProperties;

    public JwtTokenProvider(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.signingKey = buildSigningKey(applicationProperties.security().secret());
        this.jwtParser =
                Jwts.parser()
                        .verifyWith(signingKey)
                        .clockSkewSeconds(ALLOWED_CLOCK_SKEW_SECONDS)
                        .build();
    }

    public String createAccessToken(UUID userId) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plus(
                                        applicationProperties
                                                .security()
                                                .cookie()
                                                .accessToken()
                                                .ttl())))
                .signWith(signingKey)
                .compact();
    }

    public String createRefreshToken(UUID userId) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plus(
                                        applicationProperties
                                                .security()
                                                .cookie()
                                                .refreshToken()
                                                .ttl())))
                .signWith(signingKey)
                .compact();
    }

    public Optional<UUID> extractUserId(String token) {
        try {
            Claims claims = jwtParser.parseSignedClaims(token).getPayload();
            return Optional.ofNullable(claims.getSubject())
                    .filter(StringUtils::hasText)
                    .map(UUID::fromString);
        } catch (JwtException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static SecretKey buildSigningKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
