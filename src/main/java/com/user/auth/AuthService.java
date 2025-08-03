package com.user.auth;

import com.user.dto.AuthenticationRequest;
import com.user.dto.AuthenticationResponse;
import com.user.dto.RegisterRequest;
import com.user.dto.PasswordResetRequest; // New DTO
import com.user.email.dto.EmailRequest;
import com.user.email.service.EmailServiceImpl;
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
import java.time.LocalDateTime; // For OTP expiration
import java.util.Random; // For OTP generation
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailServiceImpl emailService;

    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email is already in use.");
        }

        String verificationToken = UUID.randomUUID().toString();


        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isVerified(false) // User is not verified on registration
                .verificationToken(verificationToken)
                .build();

        User savedUser = userRepository.save(user);


        String htmlContent = getHtmlContent ( verificationToken, savedUser );


        EmailRequest emailRequest = EmailRequest.builder()
                .to(savedUser.getEmail())
                .subject("Please Verify your email")
                .message(htmlContent)
                .build();

        emailService.sendEmailWitHTML(emailRequest);

        return AuthenticationResponse.builder()
                .timestamp(Instant.now())
                .message("Registration successful. Please check your email to verify your account.")
                .build();
    }

    private static String getHtmlContent ( String verificationToken, User savedUser ) {
        String verificationLink = "http://localhost:8080/api/v1/auth/verify?token=" + verificationToken;
        String htmlContent =String.format("""
           <html>
            <body>
                <h2>Welcome, %s!</h2>
                <p>Thank you for registering. Please verify your email by clicking the link below:</p>
                <a href="%s">Verify Email</a>
                <br><br>
                <p>If you did not sign up, you can ignore this email.</p>
            </body>
        </html>
        """, savedUser.getFirstname(), verificationLink);
        return htmlContent;
    }

    public AuthenticationResponse login( AuthenticationRequest request) {
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationFailedException("User not found."));


        if (!user.isVerified()) {
            throw new AuthenticationFailedException("Email not verified. Please check your inbox for the verification link.");
        }

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

    public AuthenticationResponse changePassword (String currentPassword, String newPassword, String email) {

        User user = userRepository.findByEmail ( email )
                .orElseThrow(() -> new AuthenticationFailedException("User not found."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if(passwordEncoder.matches(newPassword, user.getPassword())){
            throw new AuthenticationFailedException ( "New password should not match current password." );
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new AuthenticationResponse("Password updated successfully");
    }

    public AuthenticationResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid or expired verification token."));

        if (user.isVerified()) {
            return AuthenticationResponse.builder()
                    .timestamp(Instant.now())
                    .message("Email already verified.")
                    .build();
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return AuthenticationResponse.builder()
                .timestamp(Instant.now())
                .message("Email verified successfully. You can now log in.")
                .build();
    }

    public AuthenticationResponse forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailedException("User with this email not found."));
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now().plusMinutes(1));
        userRepository.save(user);

        String htmlContent =  getHtmlContentForOtp( user.getFirstname (), otp);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(user.getEmail())
                .subject("Password Reset OTP")
                .message(htmlContent)
                .build();

        emailService.sendEmailWitHTML(emailRequest);

        return AuthenticationResponse.builder()
                .timestamp(Instant.now())
                .message("Password reset OTP sent to your email.")
                .build();
    }
    public String getHtmlContentForOtp(String username, String otp){
      return  String.format("""
            <html>
                <body>
                    <h2>Password Reset OTP</h2>
                    <p>Hello %s,</p>
                    <p>Your One-Time Password (OTP) for resetting your password is: <strong>%s</strong></p>
                    <p>This OTP is valid for 5 minutes.</p>
                    <br>
                    <p>If you did not request a password reset, please ignore this email.</p>
                </body>
            </html>
            """, username, otp);
    }

    public AuthenticationResponse verifyOtpAndResetPassword(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationFailedException("User not found."));


        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new AuthenticationFailedException("Invalid OTP.");
        }

        if (user.getOtpGeneratedTime() == null || LocalDateTime.now().isAfter(user.getOtpGeneratedTime())) {
            throw new AuthenticationFailedException("OTP has expired. Please request a new one.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new AuthenticationFailedException("New password cannot be the same as the old password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(null);
        user.setOtpGeneratedTime(null);
        userRepository.save(user);

        return AuthenticationResponse.builder()
                .timestamp(Instant.now())
                .message("Password reset successfully.")
                .build();
    }
}