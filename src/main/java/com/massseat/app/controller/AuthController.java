package com.massseat.app.controller;

import com.massseat.app.dto.auth.*;
import com.massseat.app.dto.user.UserResponse;
import com.massseat.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "01. Authentication", description = "Register, verify email via OTP, login, refresh & password reset")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Create a new account (an otp is send the email)")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify the registration OTP; activates the account and returns tokens")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyEmail(request);
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend the registration OTP")
    public Map<String, String> resendOtp(@Valid @RequestBody EmailRequest request) {
        authService.resendRegistrationOtp(request.getEmail());
        return Map.of("message", "OTP sent");
    }


    @PostMapping("/login")
    @Operation(summary = "Login with email & password (stores approx. lat/lng from the request IP)")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authService.login(request, clientIp(httpRequest));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Send a password-reset OTP to the email (silent for unknown emails)")
    public Map<String, String> forgotPassword(@Valid @RequestBody EmailRequest request) {
        authService.forgotPassword(request.getEmail());
        return Map.of("message", "If the email exists, an OTP has been sent");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset the password using the OTP")
    public Map<String, String> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return Map.of("message", "Password has been reset. Please log in.");
    }

    /// Function: Extract request to client ip address
    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }


}
