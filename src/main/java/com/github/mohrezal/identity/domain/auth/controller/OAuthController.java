package com.github.mohrezal.identity.domain.auth.controller;

import com.github.mohrezal.identity.config.ApplicationProperties;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.auth.enums.OAuthFlowType;
import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.domain.auth.exception.type.OAuthCallbackRedirectException;
import com.github.mohrezal.identity.domain.auth.query.OAuthAuthorizeQuery;
import com.github.mohrezal.identity.domain.auth.query.OAuthCallbackQuery;
import com.github.mohrezal.identity.domain.auth.query.param.OAuthAuthorizeQueryParams;
import com.github.mohrezal.identity.domain.auth.query.param.OAuthCallbackQueryParams;
import com.github.mohrezal.identity.domain.user.model.User;
import com.github.mohrezal.identity.shared.exception.type.UnauthorizedException;
import com.github.mohrezal.identity.shared.service.ClientIpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping(RouteConstants.Auth.OAuth.BASE)
@Tag(name = "Authentication")
@RequiredArgsConstructor
public class OAuthController {

    private final OAuthAuthorizeQuery authAuthorizeQuery;
    private final OAuthCallbackQuery oAuthCallbackQuery;

    private final ClientIpService clientIpService;
    private final ApplicationProperties applicationProperties;

    @GetMapping(RouteConstants.Auth.OAuth.AUTHORIZE)
    public ResponseEntity<?> authorize(
            @PathVariable String provider, @RequestParam("redirect_url") String redirectUrl) {
        var params =
                new OAuthAuthorizeQueryParams(
                        OAuthProviderType.fromName(provider),
                        OAuthFlowType.LOGIN,
                        redirectUrl,
                        null);
        var query = authAuthorizeQuery.execute(params);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(query)).build();
    }

    @GetMapping(RouteConstants.Auth.OAuth.LINK)
    public ResponseEntity<?> link(
            @PathVariable String provider,
            @RequestParam("redirect_url") String redirectUrl,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new UnauthorizedException();
        }

        var params =
                new OAuthAuthorizeQueryParams(
                        OAuthProviderType.fromName(provider),
                        OAuthFlowType.LINK,
                        redirectUrl,
                        user.getId());
        var query = authAuthorizeQuery.execute(params);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(query)).build();
    }

    @GetMapping(RouteConstants.Auth.OAuth.CALLBACK)
    public ResponseEntity<?> callback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest request) {
        var params =
                new OAuthCallbackQueryParams(
                        OAuthProviderType.fromName(provider),
                        code,
                        state,
                        clientIpService.getClientIp(request),
                        request.getHeader(HttpHeaders.USER_AGENT));
        try {
            var response = oAuthCallbackQuery.execute(params);

            if (OAuthFlowType.LINK.equals(response.flowType())) {
                return ResponseEntity.status(HttpStatus.FOUND)
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
                            .build(
                                    response.authResponse().refreshToken(),
                                    RouteConstants.build(
                                            RouteConstants.Auth.BASE, RouteConstants.Auth.REFRESH));

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .location(URI.create(response.redirectUrl()))
                    .build();
        } catch (OAuthCallbackRedirectException exception) {
            var redirectUrl =
                    UriComponentsBuilder.fromUriString(exception.redirectUrl())
                            .replaceQueryParam("error", exception.errorCode().value())
                            .build()
                            .toUriString();

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        }
    }
}
