package com.massseat.app.service;

import com.massseat.app.entity.enums.OtpPurpose;
import com.massseat.app.exception.BadRequestException;

public interface OtpService {

    /**
     * Sends a fresh OTP after enforcing anti-abuse limits: a per-address resend
     * cooldown and a rolling hourly cap. Throws {@link BadRequestException} when a
     * limit is hit, so user-triggered resends surface the reason.
     */
    void sendOtp(String email, OtpPurpose purpose);

    /**
     * Sends an OTP but swallows a rate-limit rejection. Used where a flow must not
     * fail just because a code was already sent moments ago — e.g. a returning
     * user hammering login on a pending-deletion account. The already-sent code
     * stays valid, so nothing is lost.
     *
     * <p>Runs in its own transaction (REQUIRES_NEW): the caller — login — throws
     * an AccountStateException right after, which would otherwise roll the freshly
     * saved OTP back with it. Committing independently keeps the code valid.
     */
    void sendOtpBestEffort(String email, OtpPurpose purpose);

    /**
     * Verify the otp according to user request
     * @param email request user email
     * @param purpose which purpose user request to verify otp. likes, registration, password_reset, account_recover
     * @param code otp code
     */
    void verifyOtp(String email, OtpPurpose purpose, String code);


}
