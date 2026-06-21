package com.github.mohrezal.identity.domain.authentication.mapper;

import com.github.mohrezal.identity.domain.authentication.dto.OAuthConnectionSummary;
import com.github.mohrezal.identity.domain.authentication.model.UserOauthConnection;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserOauthConnectionMapper {
    OAuthConnectionSummary toSummary(UserOauthConnection connection);
}
