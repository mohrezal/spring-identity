package com.github.mohrezal.identity.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResendEmailVerificationRequest(@NotBlank @Email @Size(max = 100) String email) {}
