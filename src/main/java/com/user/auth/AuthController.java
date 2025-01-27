package com.user.auth;

import com.user.model.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody  RegisterRequest request
    ){
        Role role = request.getRole() != null ? request.getRole() : Role.USER;
        request.setRole(role);
        AuthenticationResponse response = service.register(request);
        return new ResponseEntity<AuthenticationResponse>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ){
        AuthenticationResponse response = service.login(request);

        return  ResponseEntity.ok(response);
    }

}
