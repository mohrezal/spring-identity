package com.github.mohrezal.identity.domain.user.controller;

import com.github.mohrezal.identity.audit.service.AuditEventFactory;
import com.github.mohrezal.identity.config.RouteConstants;
import com.github.mohrezal.identity.domain.user.command.RegisterCommand;
import com.github.mohrezal.identity.domain.user.command.param.RegisterCommandParams;
import com.github.mohrezal.identity.domain.user.dto.RegisterRequest;
import com.github.mohrezal.identity.domain.user.dto.RegisterResponse;
import com.github.mohrezal.identity.domain.user.dto.UserSummary;
import com.github.mohrezal.identity.domain.user.query.GetCurrentUserQuery;
import com.github.mohrezal.identity.domain.user.query.param.GetCurrentUserQueryParams;
import com.github.mohrezal.identity.shared.annotation.Authenticated;
import com.github.mohrezal.identity.shared.service.HttpRequestContextService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final GetCurrentUserQuery getCurrentUserQuery;
    private final HttpRequestContextService httpRequestContextService;
    private final AuditEventFactory auditEventFactory;
    private final ApplicationEventPublisher applicationEventPublisher;

    @PostMapping(RouteConstants.User.REGISTER)
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest body,
            @RequestParam("redirectUrl") String redirectUrl,
            HttpServletRequest request) {
        var auditRequestContext = auditEventFactory.createAuditRequestContext(request);
        var params = new RegisterCommandParams(body, redirectUrl);

        applicationEventPublisher.publishEvent(
                auditEventFactory.registerStarted(auditRequestContext, body.email()));

        var response = registerCommand.execute(params, auditRequestContext);

        applicationEventPublisher.publishEvent(
                auditEventFactory.registerSucceeded(
                        auditRequestContext, response.userId(), body.email()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Authenticated
    @GetMapping(RouteConstants.User.ME)
    public ResponseEntity<UserSummary> me(@AuthenticationPrincipal UserDetails userDetails) {
        var params = new GetCurrentUserQueryParams(userDetails);
        var response = getCurrentUserQuery.execute(params, null);
        return ResponseEntity.ok(response);
    }
}
