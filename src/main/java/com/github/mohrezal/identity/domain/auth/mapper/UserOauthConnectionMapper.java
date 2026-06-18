package com.github.mohrezal.identity.domain.auth.mapper;

import com.github.mohrezal.identity.domain.auth.dto.OAuthConnectionSummary;
import com.github.mohrezal.identity.domain.auth.model.UserOauthConnection;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserOauthConnectionMapper {
    OAuthConnectionSummary toSummary(UserOauthConnection connection);
}
