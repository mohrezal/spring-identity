package com.github.mohrezal.identity.domain.authentication.listener.message;

import com.github.mohrezal.identity.domain.authentication.enums.OAuthProviderType;
import java.util.UUID;

public record OAuthLinkEmailMessage(UUID userId, String to, OAuthProviderType provider) {}
