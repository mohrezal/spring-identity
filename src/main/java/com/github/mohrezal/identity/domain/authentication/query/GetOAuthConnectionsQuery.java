package com.github.mohrezal.identity.domain.authentication.query;

import com.github.mohrezal.identity.domain.authentication.dto.OAuthConnectionSummary;
import com.github.mohrezal.identity.domain.authentication.mapper.UserOauthConnectionMapper;
import com.github.mohrezal.identity.domain.authentication.query.param.GetOAuthConnectionsQueryParams;
import com.github.mohrezal.identity.domain.authentication.repository.UserOauthConnectionRepository;
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
    public List<OAuthConnectionSummary> execute(GetOAuthConnectionsQueryParams params) {
        var user = getCurrentUser(params);
        return userOauthConnectionRepository.findAllByUser(user).stream()
                .map(userOauthConnectionMapper::toSummary)
                .toList();
    }
}
