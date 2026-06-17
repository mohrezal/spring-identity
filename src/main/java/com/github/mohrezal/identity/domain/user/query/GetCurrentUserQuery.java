package com.github.mohrezal.identity.domain.user.query;

import com.github.mohrezal.identity.domain.user.dto.UserSummary;
import com.github.mohrezal.identity.domain.user.mapper.UserMapper;
import com.github.mohrezal.identity.domain.user.query.param.GetCurrentUserQueryParams;
import com.github.mohrezal.identity.shared.abstracts.AuthenticatedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentUserQuery
        extends AuthenticatedQuery<GetCurrentUserQueryParams, UserSummary> {

    private final UserMapper userMapper;

    @Override
    public UserSummary execute(GetCurrentUserQueryParams params) {
        return userMapper.toUserSummary(getCurrentUser(params));
    }
}
