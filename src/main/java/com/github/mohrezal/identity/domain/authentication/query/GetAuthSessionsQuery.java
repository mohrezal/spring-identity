package com.github.mohrezal.identity.domain.authentication.query;

import com.github.mohrezal.identity.domain.authentication.dto.SessionSummary;
import com.github.mohrezal.identity.domain.authentication.mapper.RefreshTokenMapper;
import com.github.mohrezal.identity.domain.authentication.model.RefreshToken;
import com.github.mohrezal.identity.domain.authentication.query.param.GetAuthSessionsQueryParams;
import com.github.mohrezal.identity.domain.authentication.repository.RefreshTokenRepository;
import com.github.mohrezal.identity.shared.abstracts.AuthenticatedQuery;
import com.github.mohrezal.identity.shared.service.HashService;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GetAuthSessionsQuery
        extends AuthenticatedQuery<GetAuthSessionsQueryParams, List<SessionSummary>> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenMapper refreshTokenMapper;
    private final HashService hashService;

    @Override
    public List<SessionSummary> execute(GetAuthSessionsQueryParams params) {
        var user = getCurrentUser(params);
        var currentSessionHash =
                StringUtils.hasText(params.rawRefreshToken())
                        ? hashService.hashHex(params.rawRefreshToken())
                        : null;

        return refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user).stream()
                .filter(refreshToken -> !refreshToken.isExpired())
                .sorted(Comparator.comparing(RefreshToken::getCreatedAt).reversed())
                .map(
                        refreshToken ->
                                refreshTokenMapper.toSessionSummary(
                                        refreshToken,
                                        refreshToken.getHashedToken().equals(currentSessionHash)))
                .toList();
    }
}
