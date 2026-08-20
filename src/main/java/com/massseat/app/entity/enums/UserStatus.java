package com.massseat.app.entity.enums;

public enum UserStatus {

    /** Fully usable account */
    ACTIVE,
    /** Permanently or temporarily blocked by admin or moderator */
    SUSPENDED,
    /** Permanently band the user */
    BANNED,
    /**
     * The user asked to delete their account. The row survives for a grace period
     * (see app.account.deletion-grace-days) so a returning user can recover it by
     * OTP; after that the scheduler purges it for good.
     */
    PENDING_DELETION
}
