package com.massseat.app.service;

import com.massseat.app.config.AppProperties;
import com.massseat.app.entity.OtpToken;
import com.massseat.app.entity.enums.OtpPurpose;
import com.massseat.app.exception.BadRequestException;
import com.massseat.app.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
//    private final EmailService emailService;
    private final ResendEmailService emailService;
    private final AppProperties appProperties;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void sendOtp(String email, OtpPurpose purpose) {
        String normalised = email.toLowerCase();
        enforceRateLimit(email, purpose);
        issue(normalised, purpose);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendOtpBestEffort(String email, OtpPurpose purpose) {
        try {
            String normalised = email.toLowerCase();
            enforceRateLimit(normalised, purpose);
            issue(normalised, purpose);
        } catch (BadRequestException ex) {
            log.debug("OTP for {} ({}) throttled - reusing the recent code: {}", email, purpose, ex.getMessage());
        }
    }

    public void verifyOtp(String email, OtpPurpose purpose, String code) {
        OtpToken token = otpTokenRepository
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email.toLowerCase(), purpose)
                .orElseThrow(() -> new BadRequestException("No OTP found. Please request for a new one."));

        if (token.isUsed())
            throw new BadRequestException("OTP is already used. Please request for a new one.");
        if (token.getExpiresAt().isBefore(Instant.now()))
            throw new BadRequestException("OTP has expired. Please request for a new one.");
        if (token.getAttempts() >= appProperties.getOtp().getMaxAttempts())
            throw new BadRequestException("Too many attempts. Please request for a new one.");
        if (!token.getCode().equals(code)) {
            token.setAttempts(token.getAttempts() + 1);
            otpTokenRepository.save(token);
            throw new BadRequestException("Invalid OTP code.");
        }
        token.setUsed(true);
        otpTokenRepository.save(token);
    }

    //---------------------------------------------

    private void enforceRateLimit(String email, OtpPurpose purpose) {
        AppProperties.Otp cfg = appProperties.getOtp();

        long recent = otpTokenRepository.countByEmailAndPurposeAndCreatedAtAfter(
                email, purpose, Instant.now().minus(1, ChronoUnit.HOURS)
        );
        if (recent >= cfg.getMaxPerHour())
            throw new BadRequestException("Too many verification codes requested. Please try again in an hour.");

        otpTokenRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .ifPresent(last -> {
                    long waited = Duration.between(last.getCreatedAt(), Instant.now())
                            .getSeconds();
                    long remaining = cfg.getResendCooldownSeconds() - waited;
                    if (remaining > 0)
                        throw new BadRequestException("Please wain " + remaining + " seconds before requesting another code.");
                });
    }

    private void issue(String email, OtpPurpose purpose) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        OtpToken otp = OtpToken.builder()
                .email(email)
                .code(code)
                .purpose(purpose)
                .expiresAt(Instant.now()
                        .plus(appProperties.getOtp().getExpiryMinutes(), ChronoUnit.MINUTES))
                .build();
        otpTokenRepository.save(otp);
        emailService.sendOtp(email, code, purpose.name());
    }
}
