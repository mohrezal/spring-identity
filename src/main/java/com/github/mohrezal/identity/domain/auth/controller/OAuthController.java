package com.github.mohrezal.identity.domain.auth.controller;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.auth.command.UnlinkOAuthConnectionCommand;
import com.github.mohrezal.identity.domain.auth.command.param.UnlinkOAuthConnectionCommandParams;
import com.github.mohrezal.identity.domain.auth.dto.OAuthConnectionSummary;
import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.query.GetOAuthConnectionsQuery;
import com.github.mohrezal.identity.domain.auth.query.OAuthAuthorizeQuery;
import com.github.mohrezal.identity.domain.auth.query.OAuthCallbackQuery;
import com.github.mohrezal.identity.domain.auth.query.param.GetOAuthConnectionsQueryParams;
import com.github.mohrezal.identity.domain.auth.query.param.OAuthAuthorizeQueryParams;
import com.github.mohrezal.identity.domain.auth.query.param.OAuthCallbackQueryParams;
import com.github.mohrezal.identity.domain.privilege.constant.Permissions;
import com.github.mohrezal.identity.shared.annotation.RequiresPermission;
import com.github.mohrezal.identity.shared.service.ClientIpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RouteConstants.Auth.OAuth.BASE)
@Tag(name = "Authentication")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthAuthorizeQuery authAuthorizeQuery;
    private final OAuthCallbackQuery oAuthCallbackQuery;
    private final GetOAuthConnectionsQuery getOAuthConnectionsQuery;
    private final UnlinkOAuthConnectionCommand unlinkOAuthConnectionCommand;

    private final ClientIpService clientIpService;
    private final ApplicationProperties applicationProperties;

    @RequiresPermission(Permissions.IDENTITY_AUTH_OAUTH_CONNECTIONS_READ)
    @GetMapping(RouteConstants.Auth.OAuth.CONNECTIONS)
    public ResponseEntity<List<OAuthConnectionSummary>> connections(
            @AuthenticationPrincipal UserDetails userDetails) {
        var params = new GetOAuthConnectionsQueryParams(userDetails);
        var response = getOAuthConnectionsQuery.execute(params);
        return ResponseEntity.ok(response);
    }

    @RequiresPermission(Permissions.IDENTITY_AUTH_OAUTH_CONNECTIONS_UNLINK)
    @DeleteMapping(RouteConstants.Auth.OAuth.CONNECTION)
    public ResponseEntity<Void> unlinkConnection(
            @PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        var params = new UnlinkOAuthConnectionCommandParams(userDetails, id);
        unlinkOAuthConnectionCommand.execute(params);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(RouteConstants.Auth.OAuth.AUTHORIZE)
    public ResponseEntity<?> authorize(
            @PathVariable String provider, @RequestParam("redirect_url") String redirectUrl) {
        var params =
                new OAuthAuthorizeQueryParams(
                        OAuthProviderType.fromName(provider),
                        OAuthFlowType.LOGIN,
                        redirectUrl,
                        null);
        var response = authAuthorizeQuery.execute(params);
        var stateCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .oauthState()
                        .build(response.correlationId());
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, stateCookie.toString())
                .location(URI.create(response.authorizationUrl()))
                .build();
    }

    @RequiresPermission(Permissions.IDENTITY_AUTH_OAUTH_CONNECTIONS_LINK)
    @GetMapping(RouteConstants.Auth.OAuth.LINK)
    public ResponseEntity<?> link(
            @PathVariable String provider,
            @RequestParam("redirect_url") String redirectUrl,
            @AuthenticationPrincipal UserDetails userDetails) {
        var params =
                new OAuthAuthorizeQueryParams(
                        OAuthProviderType.fromName(provider),
                        OAuthFlowType.LINK,
                        redirectUrl,
                        userDetails);
        var response = authAuthorizeQuery.execute(params);
        var stateCookie =
                applicationProperties
                        .security()
                        .cookie()
                        .oauthState()
                        .build(response.correlationId());
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, stateCookie.toString())
                .location(URI.create(response.authorizationUrl()))
                .build();
    }

    @GetMapping(RouteConstants.Auth.OAuth.CALLBACK)
    public ResponseEntity<?> callback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest request) {
        var clearStateCookie = applicationProperties.security().cookie().oauthState().clear();
        var params =
                new OAuthCallbackQueryParams(
                        OAuthProviderType.fromName(provider),
                        code,
                        state,
                        applicationProperties
                                .security()
                                .cookie()
                                .oauthState()
                                .valueFrom(request.getCookies()),
                        clientIpService.getClientIp(request),
                        request.getHeader(HttpHeaders.USER_AGENT));
        var response = oAuthCallbackQuery.execute(params);

        if (OAuthFlowType.LINK.equals(response.flowType())) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, clearStateCookie.toString())
                    .location(URI.create(response.redirectUrl()))
                    .build();
        }

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

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .header(HttpHeaders.SET_COOKIE, clearStateCookie.toString())
                .location(URI.create(response.redirectUrl()))
                .build();
    }
}
