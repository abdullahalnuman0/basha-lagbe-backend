package com.massseat.app.service.impl;

import com.massseat.app.dto.auth.*;
import com.massseat.app.dto.user.UserResponse;
import com.massseat.app.entity.User;
import com.massseat.app.entity.enums.OtpPurpose;
import com.massseat.app.exception.BadRequestException;
import com.massseat.app.exception.ResourceNotFoundException;
import com.massseat.app.exception.UnauthorizedException;
import com.massseat.app.repository.UserRepository;
import com.massseat.app.service.AuthService;
import com.massseat.app.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest req) {
        String email = req.getEmail().toLowerCase();
        if (userRepository.existsByEmail(email))
            throw new BadRequestException("Email already exists");

        User user = User.builder()
                .fullName(req.getFullName())
                .email(email)
                .phone(req.getPhone())
                .passwordHash(req.getPassword()) // When implement Security, then password save in hash formate!
                .gender(req.getGender())
                .division(req.getDivision())
                .district(req.getDistrict())
                .area(req.getArea())
                .institution(req.getInstitution())
                .termsAcceptedAt(Instant.now())
                .build();
        userRepository.save(user);
        otpService.sendOtp(email, OtpPurpose.REGISTRATION);

        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public AuthResponse verifyEmail(VerifyOtpRequest req) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email " + req.getEmail()));

        if (user.isEmailVerified())
            throw new BadRequestException("Email already verified");

        otpService.verifyOtp(req.getEmail(), OtpPurpose.REGISTRATION, req.getCode());
        user.setEmailVerified(true);
        userRepository.save(user);

        return AuthResponse.builder()
                .user(UserResponse.from(user))
                .build();
    }

    @Override
    @Transactional
    public void resendRegistrationOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email " + email));

        if (user.isEmailVerified())
            throw new BadRequestException("Email already verified");

        otpService.sendOtp(email, OtpPurpose.REGISTRATION);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req, String clientId) {

        String email = req.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        // change that logic when implement security
        if (!req.getPassword().equals(user.getPasswordHash()))
            throw new UnauthorizedException("Invalid email or password");

        if (!user.isEmailVerified())
            throw new UnauthorizedException("Email is not verified. Please verify your email.");

        // more work when implement security

        return AuthResponse.builder()
                .user(UserResponse.from(user))
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email.toLowerCase())
                .ifPresent(user -> otpService.sendOtp(user.getEmail(), OtpPurpose.PASSWORD_RESET));
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest req) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found for this email"));
        otpService.verifyOtp(req.getEmail(),OtpPurpose.PASSWORD_RESET, req.getCode() );
        user.setPasswordHash(req.getNewPassword()); // saved the pass convert to hash/ security
        userRepository.save(user);
    }
}
