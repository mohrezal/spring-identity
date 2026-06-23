package com.github.mohrezal.identity.domain.auth.service.oauth;

import com.github.mohrezal.identity.domain.auth.enums.OAuthProviderType;
import com.github.mohrezal.identity.shared.exception.type.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OAuthProviderRegistry {
    private final Map<OAuthProviderType, OAuthProvider> providers;

    public OAuthProviderRegistry(List<OAuthProvider> providers) {
        this.providers =
                providers.stream()
                        .collect(Collectors.toMap(OAuthProvider::provider, Function.identity()));

        log.info("Registered OAuth providers. providers={}", this.providers.keySet());
    }

    public OAuthProvider get(OAuthProviderType type) {
        var provider = providers.get(type);
        if (provider == null) {
            throw new NotFoundException();
        }
        return provider;
    }
}
