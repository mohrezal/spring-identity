package com.github.mohrezal.identity.domain.auth.query;

import com.github.mohrezal.identity.audit.service.AuditRequestContext;
import com.github.mohrezal.identity.domain.auth.dto.OAuthConnectionSummary;
import com.github.mohrezal.identity.domain.auth.mapper.UserOauthConnectionMapper;
import com.github.mohrezal.identity.domain.auth.query.param.GetOAuthConnectionsQueryParams;
import com.github.mohrezal.identity.domain.auth.repository.UserOauthConnectionRepository;
import com.github.mohrezal.identity.shared.abstracts.AuthenticatedQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetOAuthConnectionsQuery
        extends AuthenticatedQuery<GetOAuthConnectionsQueryParams, List<OAuthConnectionSummary>> {

    private final UserOauthConnectionRepository userOauthConnectionRepository;
    private final UserOauthConnectionMapper userOauthConnectionMapper;

    @Override
    public List<OAuthConnectionSummary> execute(
            GetOAuthConnectionsQueryParams params, AuditRequestContext auditRequestContext) {
        var user = getCurrentUser(params);
        return userOauthConnectionRepository.findAllByUser(user).stream()
                .map(userOauthConnectionMapper::toSummary)
                .toList();
    }
}
