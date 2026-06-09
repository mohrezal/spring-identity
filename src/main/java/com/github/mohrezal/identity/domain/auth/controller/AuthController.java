package com.github.mohrezal.identity.domain.auth.controller;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.auth.command.VerifyEmailCommand;
import com.github.mohrezal.identity.domain.auth.command.param.VerifyEmailCommandParams;
import com.github.mohrezal.identity.domain.auth.dto.CsrfTokenResponse;
import io.swagger.v3.oas.annotations.Parameter;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RouteConstants.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    private final VerifyEmailCommand verifyEmailCommand;

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
}
