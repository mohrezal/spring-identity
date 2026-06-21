package com.github.mohrezal.identity.domain.authentication.mapper;

import com.github.mohrezal.identity.domain.authentication.dto.SessionSummary;
import com.github.mohrezal.identity.domain.authentication.model.RefreshToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RefreshTokenMapper {
    @Mapping(target = "isCurrentSession", source = "isCurrentSession")
    SessionSummary toSessionSummary(RefreshToken refreshToken, boolean isCurrentSession);
}
