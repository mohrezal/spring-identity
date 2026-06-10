package com.github.mohrezal.identity.domain.auth.controller;

import com.github.mohrezal.identity.config.RouteConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RouteConstants.Auth.OAUTH_BASE)
@Tag(name = "Authentication")
public class OAuthController {
    @GetMapping(RouteConstants.Auth.OAUTH_GOOGLE)
    public ResponseEntity<?> google() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
