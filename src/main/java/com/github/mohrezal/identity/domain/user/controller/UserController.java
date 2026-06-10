package com.github.mohrezal.identity.domain.user.controller;

import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.user.command.RegisterCommand;
import com.github.mohrezal.identity.domain.user.command.param.RegisterCommandParams;
import com.github.mohrezal.identity.domain.user.dto.RegisterRequest;
import com.github.mohrezal.identity.domain.user.dto.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(RouteConstants.User.BASE)
@RequiredArgsConstructor
public class UserController {

    private final RegisterCommand registerCommand;

    @PostMapping(RouteConstants.User.REGISTER)
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest body,
            @RequestParam("redirectUrl") String redirectUrl) {
        var params = new RegisterCommandParams(body, redirectUrl);

        var response = registerCommand.execute(params);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
