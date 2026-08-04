package com.github.mohrezal.identity.domain.auth.dto;

import com.github.mohrezal.identity.shared.service.EmailAddressNormalizer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(@NotBlank @Email @Size(max = 100) String email) {

    public ForgotPasswordRequest {
        email = EmailAddressNormalizer.normalize(email);
    }
}
