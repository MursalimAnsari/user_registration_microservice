package com.user.auth;

import com.user.exception.AuthenticationFailedException;
import com.user.exception.UserAlreadyExistsException;
import com.user.model.User;
import com.user.repository.UserRepository;
import com.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already in use.");
        }

        User user = User
                .builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);
        String jwtToken = jwtService.generateToken(savedUser);
        return AuthenticationResponse
                .builder()
                .accessToken(jwtToken)
                .timestamp(Instant.now())
                .message("User registered successfully.")
                .build();
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationFailedException("User not found."));

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    ));
        } catch (AuthenticationException ex) {
            throw new AuthenticationFailedException("Invalid email or password.");
        }

        String jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse
                .builder()
                .accessToken(jwtToken)
                .timestamp(Instant.now())
                .message("User logged in successfully.")
                .build();
    }
}
