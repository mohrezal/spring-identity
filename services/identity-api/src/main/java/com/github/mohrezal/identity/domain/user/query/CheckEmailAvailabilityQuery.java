package com.github.mohrezal.identity.domain.user.query;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.user.dto.EmailAvailabilityResponse;
import com.github.mohrezal.identity.domain.user.query.param.CheckEmailAvailabilityQueryParams;
import com.github.mohrezal.identity.domain.user.repository.UserRepository;
import com.github.mohrezal.identity.shared.interfaces.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckEmailAvailabilityQuery
        implements Query<CheckEmailAvailabilityQueryParams, EmailAvailabilityResponse> {

    private final UserRepository userRepository;

    @Override
    public EmailAvailabilityResponse execute(
            CheckEmailAvailabilityQueryParams params, AuditRequestContext auditRequestContext) {
        var email = params.email();
        var available = !userRepository.existsUserByEmail(email);
        return new EmailAvailabilityResponse(email, available);
    }
}
