package com.github.mohrezal.identity.domain.auth.controller;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.auth.dto.CsrfTokenResponse;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RouteConstants.Auth.BASE)
@RequiredArgsConstructor
public class AuthController {

    @GetMapping(RouteConstants.Auth.CSRF)
    public ResponseEntity<CsrfTokenResponse> csrf(@Parameter(hidden = true) CsrfToken csrfToken) {
        return ResponseEntity.ok(
                new CsrfTokenResponse(csrfToken.getToken(), csrfToken.getHeaderName()));
    }
}
