package com.user.auth;

import com.user.dto.AuthenticationRequest;
import com.user.dto.AuthenticationResponse;
import com.user.dto.PasswordChangeRequest;
import com.user.dto.RegisterRequest;
import com.user.dto.PasswordResetRequest; // New DTO
import com.user.model.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ){
        Role role = request.getRole() != null ? request.getRole() : Role.USER;
        request.setRole(role);
        AuthenticationResponse response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ){
        AuthenticationResponse response = userService.login(request);

        return  ResponseEntity.ok(response);
    }


    /*
       Old method to update password
      */
    @PostMapping("/update-password/{email}")
    public ResponseEntity<?> updatePassword(
            @RequestBody PasswordChangeRequest request,
            @PathVariable("email") String email
    ){
        AuthenticationResponse response =
                userService.changePassword(request.getCurrentPassword (), request.getNewPassword (), email);

        return  ResponseEntity.ok(response);
    }

    @GetMapping("/verify")
    public ResponseEntity<AuthenticationResponse> verifyEmail(@RequestParam("token") String token) {
        AuthenticationResponse response = userService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/forgot-password/{email}")
    public ResponseEntity<AuthenticationResponse> forgotPassword(@PathVariable("email") String email) {
        AuthenticationResponse response = userService.forgotPassword(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthenticationResponse> resetPassword(@RequestBody PasswordResetRequest request) {
        AuthenticationResponse response = userService.verifyOtpAndResetPassword(request);
        return ResponseEntity.ok(response);
    }
}