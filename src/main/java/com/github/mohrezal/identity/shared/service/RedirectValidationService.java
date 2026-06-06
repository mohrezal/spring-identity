package com.github.mohrezal.identity.shared.service;

import com.github.mohrezal.identity.config.ApplicationProperties;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedirectValidationService {

    private final ApplicationProperties applicationProperties;

    public boolean isValid(String redirectUrl) {
        if (redirectUrl == null || redirectUrl.isBlank()) {
            return false;
        }

        try {
            var uri = new URI(redirectUrl).normalize();

            if (uri.getScheme() == null || uri.getHost() == null) {
                return false;
            }

            var origin = uri.getScheme() + "://" + uri.getHost();
            if (uri.getPort() != -1) origin += ":" + uri.getPort();

            return applicationProperties.security().allowedOrigin().contains(origin);

        } catch (URISyntaxException e) {
            log.warn("Invalid redirect URL: {}", redirectUrl);
            return false;
        }
    }
}
