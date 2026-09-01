package com.massseat.app.service;

import com.massseat.app.config.AppProperties;
import com.massseat.app.dto.auth.*;
import com.massseat.app.dto.user.UserResponse;
import com.massseat.app.entity.RefreshToken;
import com.massseat.app.entity.User;
import com.massseat.app.entity.enums.OtpPurpose;
import com.massseat.app.exception.AccountStateException;
import com.massseat.app.exception.BadRequestException;
import com.massseat.app.exception.ResourceNotFoundException;
import com.massseat.app.exception.UnauthorizedException;
import com.massseat.app.repository.RefreshTokenRepository;
import com.massseat.app.repository.UserRepository;
import com.massseat.app.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    @Transactional(readOnly = true)
    public void emailVerify(@Valid EmailRequest req) {
        String email = req.getEmail().toLowerCase();
        if (userRepository.existsByEmail(email))
            throw new BadRequestException("Email already exists");
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        String email = req.getEmail().toLowerCase();
        if (userRepository.existsByEmail(email))
            throw new BadRequestException("Email already exists");

        User user = User.builder()
                .fullName(req.getFullName())
                .email(email)
                .phone(req.getPhone())
                .passwordHash(
                        passwordEncoder.encode(req.getPassword())
                )
                .gender(req.getGender())
                .roles(Set.of(req.getRole()))
                .division(trimToNull(req.getDivision()))
                .district(trimToNull(req.getDistrict()))
                .area(trimToNull(req.getArea()))
                .institution(req.getInstitution())
                .termsAcceptedAt(Instant.now())
                .build();
        userRepository.save(user);
        otpService.sendOtp(email, OtpPurpose.REGISTRATION);

        return UserResponse.from(user);
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyOtpRequest req) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email " + req.getEmail()));

        if (user.isEmailVerified())
            throw new BadRequestException("Email already verified");

        otpService.verifyOtp(req.getEmail(), OtpPurpose.REGISTRATION, req.getCode());
        user.setEmailVerified(true);
        userRepository.save(user);

        return buildAuthResponse(user);
    }


    @Transactional
    public void resendRegistrationOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email " + email));

        if (user.isEmailVerified())
            throw new BadRequestException("Email already verified");

        otpService.sendOtp(email, OtpPurpose.REGISTRATION);
    }

    @Transactional
    public AuthResponse login(LoginRequest req, String clientId) {

        String email = req.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash()))
            throw new UnauthorizedException("Invalid email or password");

        if (!user.isEmailVerified())
            throw new AccountStateException(AccountStateException.EMAIL_NOT_VERIFIED, "Email is not verified. Please verify your email.");

        // more work when implement security

        user.setLastLoginAt(Instant.now());
        user.setLastSeenAt(Instant.now());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email " + email));

        otpService.sendOtp(user.getEmail(), OtpPurpose.PASSWORD_RESET);
    }

    @Transactional
    public void resetPassword(PasswordResetRequest req) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this email"));
        otpService.verifyOtp(req.getEmail(), OtpPurpose.PASSWORD_RESET, req.getCode());
        user.setPasswordHash(req.getNewPassword()); // saved the pass convert to hash/ security
        userRepository.save(user);
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (refreshToken.isRevoked())
            throw new UnauthorizedException("Refresh token is expired or revoked, Please login again");

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        return buildAuthResponse(refreshToken.getUser());
    }


    // --- Helper methods ---
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now()
                        .plus(appProperties.getJwt().getRefreshTokenTtl().toDays(), ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .user(UserResponse.from(user))
                .build();
    }

    private static String trimToNull(String val) {
        return StringUtils.hasText(val) ? val.trim() : null;
    }


}
