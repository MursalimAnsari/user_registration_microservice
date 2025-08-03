package com.user.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record EmailRequest(@NotBlank @Email String to, @NotBlank String subject, @NotBlank String message){ }