package com.github.mohrezal.identity.domain.auth.query;

import com.github.mohrezal.identity.domain.auth.dto.SessionSummary;
import com.github.mohrezal.identity.domain.auth.mapper.RefreshTokenMapper;
import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import com.github.mohrezal.identity.domain.auth.query.param.GetAuthSessionsQueryParams;
import com.github.mohrezal.identity.domain.auth.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.shared.abstracts.AuthenticatedQuery;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAuthSessionsQuery
        extends AuthenticatedQuery<GetAuthSessionsQueryParams, List<SessionSummary>> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    public List<SessionSummary> execute(GetAuthSessionsQueryParams params) {
        var user = getCurrentUser(params);
        return refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user).stream()
                .filter(refreshToken -> !refreshToken.isExpired())
                .sorted(Comparator.comparing(RefreshToken::getCreatedAt).reversed())
                .map(refreshTokenMapper::toSessionSummary)
                .toList();
    }
}
