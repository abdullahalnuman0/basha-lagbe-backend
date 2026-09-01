package com.massseat.app.exception;

import lombok.Getter;

@Getter
public class AccountStateException extends RuntimeException {
    public static final String EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";
    public static final String SUSPENDED = "ACCOUNT_SUSPENDED";
    public static final String BANNED = "ACCOUNT_BANNED";
    public static final String PENDING_DELETION = "ACCOUNT_PENDING_DELETION";

    private final String code;

    public AccountStateException(String code, String message) {
        super(message);
        this.code = code;
    }
}
