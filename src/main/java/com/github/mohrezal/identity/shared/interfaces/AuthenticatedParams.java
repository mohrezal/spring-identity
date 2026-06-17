package com.github.mohrezal.identity.shared.interfaces;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticatedParams {
    UserDetails getUserDetails();
}
