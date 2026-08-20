package com.massseat.app.service;

import com.massseat.app.dto.auth.*;
import com.massseat.app.dto.user.UserResponse;

public interface AuthService {

    /**
     * Registers a new user account.
     *
     * @param req the registration request containing the user's account information
     * @return the registered user's response
     */
    UserResponse register(RegisterRequest req);

    /**
     * Verifies the user's email address using the provided OTP and completes
     * the account registration process.
     *
     * @param req the verification request containing the OTP information
     * @return an authentication response containing the user's authentication details
     */
    AuthResponse verifyEmail(VerifyOtpRequest req);

    /**
     * Again send the oto, if not found or expire otp
     *
     * @param email user account email, that email send again otp
     */
    void resendRegistrationOtp(String email);

    /**
     * Login the user
     *
     * @param req      the user login info
     * @param clientId the user request ip
     * @return an authentication response containing the user's authentication details
     */
    AuthResponse login(LoginRequest req, String clientId);

    /**
     * Initiates the password reset process for the specified email address.
     *
     * @param email the email address associated with the user account
     */
    void forgotPassword(String email);

    /**
     * Resets the user's password after verifying the provided OTP.
     *
     * @param req the password reset request containing the email, OTP,
     *            and new password
     */
    void resetPassword(PasswordResetRequest req);

}
