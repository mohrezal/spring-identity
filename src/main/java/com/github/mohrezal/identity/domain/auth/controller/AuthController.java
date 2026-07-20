package com.github.mohrezal.identity.domain.auth.controller;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.auth.command.ChangePasswordCommand;
import com.github.mohrezal.identity.domain.auth.command.ForgotPasswordCommand;
import com.github.mohrezal.identity.domain.auth.command.LoginCommand;
import com.github.mohrezal.identity.domain.auth.command.LogoutAllCommand;
import com.github.mohrezal.identity.domain.auth.command.LogoutCommand;
import com.github.mohrezal.identity.domain.auth.command.RefreshTokenCommand;
import com.github.mohrezal.identity.domain.auth.command.ResendEmailVerificationCommand;
import com.github.mohrezal.identity.domain.auth.command.ResetPasswordCommand;
import com.github.mohrezal.identity.domain.auth.command.RevokeAuthSessionCommand;
import com.github.mohrezal.identity.domain.auth.command.VerifyEmailCommand;
import com.github.mohrezal.identity.domain.auth.command.param.ChangePasswordCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.ForgotPasswordCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.LoginCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.LogoutAllCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.LogoutCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.RefreshTokenCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.ResendEmailVerificationCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.ResetPasswordCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.RevokeAuthSessionCommandParams;
import com.github.mohrezal.identity.domain.auth.command.param.VerifyEmailCommandParams;
import com.github.mohrezal.identity.domain.auth.dto.ChangePasswordRequest;
import com.github.mohrezal.identity.domain.auth.dto.CsrfTokenResponse;
import com.github.mohrezal.identity.domain.auth.dto.ForgotPasswordRequest;
import com.github.mohrezal.identity.domain.auth.dto.LoginRequest;
import com.github.mohrezal.identity.domain.auth.dto.ResendEmailVerificationRequest;
import com.github.mohrezal.identity.domain.auth.dto.ResetPasswordRequest;
import com.github.mohrezal.identity.domain.auth.dto.SessionSummary;
import com.github.mohrezal.identity.domain.auth.query.GetAuthSessionsQuery;
import com.github.mohrezal.identity.domain.auth.query.param.GetAuthSessionsQueryParams;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.domain.user.dto.UserSummary;
import com.github.mohrezal.identity.shared.annotation.Authenticated;
import com.github.mohrezal.identity.shared.annotation.RequiresPermission;
import com.github.mohrezal.identity.shared.constant.CookieConstant;
import com.github.mohrezal.identity.shared.service.ClientIpService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final LogoutCommand logoutCommand;
    private final LogoutAllCommand logoutAllCommand;
    private final RefreshTokenCommand refreshTokenCommand;
    private final ChangePasswordCommand changePasswordCommand;
    private final ForgotPasswordCommand forgotPasswordCommand;
    private final ResetPasswordCommand resetPasswordCommand;
    private final GetAuthSessionsQuery getAuthSessionsQuery;
    private final RevokeAuthSessionCommand revokeAuthSessionCommand;

    private final ClientIpService clientIpService;
    private final ApplicationProperties applicationProperties;
    private final CookieCsrfTokenRepository csrfTokenRepository;

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
                        .build(response.authResponse().refreshToken(), RouteConstants.Auth.BASE);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response.userSummary());
    }

    @PostMapping(RouteConstants.Auth.LOGOUT)
    public ResponseEntity<?> logout(
            @Parameter(hidden = true)
                    @CookieValue(name = CookieConstant.REFRESH_TOKEN, required = false)
                    String rawRefreshToken) {
        var params = new LogoutCommandParams(rawRefreshToken);
        logoutCommand.execute(params);

        var accessCookie = applicationProperties.security().cookie().accessToken().clear();
        var refreshCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .refreshToken()
                        .clear(RouteConstants.Auth.BASE);
        var legacyRefreshCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .refreshToken()
                        .clear(
                                RouteConstants.build(
                                        RouteConstants.Auth.BASE, RouteConstants.Auth.REFRESH));

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, legacyRefreshCookie.toString())
                .build();
    }

    @RequiresPermission(Permissions.IDENTITY_AUTH_SESSIONS_REVOKE_ALL)
    @PostMapping(RouteConstants.Auth.LOGOUT_ALL)
    public ResponseEntity<?> logoutAll(@AuthenticationPrincipal UserDetails userDetails) {
        var params = new LogoutAllCommandParams(userDetails);
        logoutAllCommand.execute(params);

        var accessCookie = applicationProperties.security().cookie().accessToken().clear();
        var refreshCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .refreshToken()
                        .clear(RouteConstants.Auth.BASE);
        var legacyRefreshCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .refreshToken()
                        .clear(
                                RouteConstants.build(
                                        RouteConstants.Auth.BASE, RouteConstants.Auth.REFRESH));

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, legacyRefreshCookie.toString())
                .build();
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
                        .build(response.refreshToken(), RouteConstants.Auth.BASE);
        var legacyRefreshCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .refreshToken()
                        .clear(
                                RouteConstants.build(
                                        RouteConstants.Auth.BASE, RouteConstants.Auth.REFRESH));
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, legacyRefreshCookie.toString())
                .build();
    }

    @PostMapping(RouteConstants.Auth.FORGOT_PASSWORD)
    public ResponseEntity<Boolean> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest body,
            @RequestParam(value = "redirectUrl") String redirectUrl) {
        var params = new ForgotPasswordCommandParams(body, redirectUrl);
        var response = forgotPasswordCommand.execute(params);
        return ResponseEntity.ok(response);
    }

    @PostMapping(RouteConstants.Auth.RESET_PASSWORD)
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest body,
            @RequestParam(value = "redirectUrl") String redirectUrl) {
        var params = new ResetPasswordCommandParams(body, redirectUrl);
        resetPasswordCommand.execute(params);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    @RequiresPermission(Permissions.IDENTITY_AUTH_SESSIONS_READ)
    @GetMapping(RouteConstants.Auth.SESSIONS)
    public ResponseEntity<List<SessionSummary>> sessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(hidden = true)
                    @CookieValue(name = CookieConstant.REFRESH_TOKEN, required = false)
                    String rawRefreshToken) {
        var params = new GetAuthSessionsQueryParams(userDetails, rawRefreshToken);
        var response = getAuthSessionsQuery.execute(params);
        return ResponseEntity.ok(response);
    }

    @RequiresPermission(Permissions.IDENTITY_AUTH_SESSIONS_REVOKE)
    @DeleteMapping(RouteConstants.Auth.SESSIONS + "/{id}")
    public ResponseEntity<?> revokeSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Parameter(hidden = true)
                    @CookieValue(name = CookieConstant.REFRESH_TOKEN, required = false)
                    String rawRefreshToken) {
        var params = new RevokeAuthSessionCommandParams(userDetails, id, rawRefreshToken);
        revokeAuthSessionCommand.execute(params);
        return ResponseEntity.noContent().build();
    }

    @Authenticated
    @PostMapping(RouteConstants.Auth.CHANGE_PASSWORD)
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest body) {
        var params = new ChangePasswordCommandParams(userDetails, body);
        changePasswordCommand.execute(params);
        return ResponseEntity.noContent().build();
    }
}
