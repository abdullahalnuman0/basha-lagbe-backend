package com.massseat.app.repository;

import com.massseat.app.entity.OtpToken;
import com.massseat.app.entity.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    /** How many OTPs have been sent to this address for each purpose since the hourly cap reset */
    long countByEmailAndPurposeAndCreatedAtAfter(String email, OtpPurpose purpose, Instant since);

    /// Mose recent send of any status - drives the resend cooldown
    Optional<OtpToken> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, OtpPurpose purpose);

}
