package com.github.mohrezal.identity.domain.auth.mapper;

import com.github.mohrezal.identity.domain.auth.dto.SessionSummary;
import com.github.mohrezal.identity.domain.auth.model.RefreshToken;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RefreshTokenMapper {
    SessionSummary toSessionSummary(RefreshToken refreshToken);
}
