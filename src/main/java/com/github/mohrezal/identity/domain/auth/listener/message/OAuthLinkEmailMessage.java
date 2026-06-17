package com.github.mohrezal.identity.domain.auth.listener.message;

import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import java.util.UUID;

public record OAuthLinkEmailMessage(UUID userId, String to, OAuthProviderType provider) {}
