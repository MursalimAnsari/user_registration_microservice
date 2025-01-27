package com.user.auth;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.user.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotEmpty(message = "firstname is required")
    @NotBlank(message = "firstname is required")
    private String firstname;
    @NotEmpty(message = "lastname is required")
    @NotBlank(message = "lastname is required")
    private String lastname;
    @Email(message = "email is not correct")
    @NotBlank(message = "email is required")
    private String email;
    @NotEmpty(message = "password is required")
    @NotBlank(message = "password is required")
    @Size(min = 5, max = 12, message = "password is not correct")
    private String password;
    private Role role;

}
