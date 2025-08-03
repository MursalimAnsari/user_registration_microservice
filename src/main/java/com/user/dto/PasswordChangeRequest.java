package com.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PasswordChangeRequest {
    @NotEmpty(message = "password is required")
    @NotBlank(message = "password is required")
    @Size(min = 5, max = 12, message = "password is not correct")
    private String currentPassword;

    @NotEmpty(message = "password is required")
    @NotBlank(message = "password is required")
    @Size(min = 5, max = 12, message = "password is not correct")
    private String newPassword;


}
