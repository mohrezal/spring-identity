package com.github.mohrezal.identity.domain.auth.controller;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.auth.command.LoginCommand;
import com.github.mohrezal.identity.domain.auth.command.RefreshTokenCommand;
import com.github.mohrezal.identity.domain.auth.command.ResendEmailVerificationCommand;
import com.github.mohrezal.identity.domain.auth.command.VerifyEmailCommand;
import com.github.mohrezal.identity.domain.auth.command.param.LoginCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.RefreshTokenCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.ResendEmailVerificationCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.VerifyEmailCommandParams;
import com.github.mohrezal.identity.domain.auth.dto.CsrfTokenResponse;
import com.github.mohrezal.identity.domain.auth.dto.LoginRequest;
import com.github.mohrezal.identity.domain.auth.dto.ResendEmailVerificationRequest;
import com.github.mohrezal.identity.domain.user.dto.UserSummary;
import com.github.mohrezal.identity.shared.constant.CookieConstant;
import com.github.mohrezal.identity.shared.service.ClientIpService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RouteConstants.Auth.BASE)
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final VerifyEmailCommand verifyEmailCommand;
    private final ResendEmailVerificationCommand resendEmailVerificationCommand;
    private final LoginCommand loginCommand;
    private final RefreshTokenCommand refreshTokenCommand;

    private final ClientIpService clientIpService;
    private final ApplicationProperties applicationProperties;

    @GetMapping(RouteConstants.Auth.CSRF)
    public ResponseEntity<CsrfTokenResponse> csrf(@Parameter(hidden = true) CsrfToken csrfToken) {
        return ResponseEntity.ok(
                new CsrfTokenResponse(csrfToken.getToken(), csrfToken.getHeaderName()));
    }

    @GetMapping(RouteConstants.Auth.VERIFY_EMAIL)
    public ResponseEntity<?> verifyEmail(
            @RequestParam("token") UUID token,
            @RequestParam(value = "redirectUrl") String redirectUrl) {
        var params = new VerifyEmailCommandParams(token, redirectUrl);
        verifyEmailCommand.execute(params);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    @PostMapping(RouteConstants.Auth.RESEND_EMAIL_VERIFICATION)
    public ResponseEntity<Boolean> resendEmailVerification(
            @Valid @RequestBody ResendEmailVerificationRequest body,
            @RequestParam(value = "redirectUrl") String redirectUrl) {
        var params = new ResendEmailVerificationCommandParams(body, redirectUrl);
        var response = resendEmailVerificationCommand.execute(params);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(RouteConstants.Auth.LOGIN)
    public ResponseEntity<UserSummary> login(
            @Valid @RequestBody LoginRequest body, HttpServletRequest request) {
        var params =
                new LoginCommandParams(
                        body,
                        clientIpService.getClientIp(request),
                        request.getHeader(HttpHeaders.USER_AGENT));

        var response = loginCommand.execute(params);
        var accessCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .accessToken()
                        .build(response.authResponse().accessToken());
        var refreshCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .refreshToken()
                        .build(
                                response.authResponse().refreshToken(),
                                RouteConstants.build(
                                        RouteConstants.Auth.BASE, RouteConstants.Auth.REFRESH));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response.userSummary());
    }

    @PostMapping(RouteConstants.Auth.REFRESH)
    public ResponseEntity<?> refresh(
            @Parameter(hidden = true)
                    @CookieValue(name = CookieConstant.REFRESH_TOKEN, required = false)
                    String rawRefreshToken,
            HttpServletRequest request) {
        var params =
                new RefreshTokenCommandParams(
                        rawRefreshToken,
                        clientIpService.getClientIp(request),
                        request.getHeader(HttpHeaders.USER_AGENT));
        var response = refreshTokenCommand.execute(params);
        var accessCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .accessToken()
                        .build(response.accessToken());
        var refreshCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .refreshToken()
                        .build(
                                response.refreshToken(),
                                RouteConstants.build(
                                        RouteConstants.Auth.BASE, RouteConstants.Auth.REFRESH));
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }
}
