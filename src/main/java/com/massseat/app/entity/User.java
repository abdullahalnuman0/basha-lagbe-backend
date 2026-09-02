package com.massseat.app.entity;


import com.massseat.app.entity.enums.Gender;
import com.massseat.app.entity.enums.Role;
import com.massseat.app.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private String passwordHash;

    @Builder.Default
    @Column(nullable = false)
    private boolean emailVerified = false;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 100)
    private String division;

    @Column(length = 100)
    private String district;

    @Column(length = 150)
    private String area;

    @Column(length = 150)
    private String institution;

    @Column(length = 500)
    private String avatarUrl;

    /**
     * When the user accepted the Terms & Privacy Policy at registration — the proof of consent.
     */
    private Instant termsAcceptedAt;

    // ---- Account lifecycle: self-service deletion ----

    /**
     * When the user requested account deletion (status becomes PENDING_DELETION).
     */
    private Instant deletionRequestedAt;

    /**
     * When the grace period ends and the scheduler permanently purges the account.
     */
    private Instant deletionScheduledAt;

    // ---- Account lifecycle: admin suspension / ban ----

    /**
     * When the current suspension started.
     */
    private Instant suspensionStartAt;

    /**
     * When the suspension auto-lifts. Null while SUSPENDED means a permanent suspension.
     */
    private Instant suspensionEndAt;

    /**
     * Admin's reason for the current suspension or ban — shown to the user and emailed.
     */
    @Column(length = 500)
    private String suspensionReason;

    /**
     * When the account was last restored (recovered from deletion, or un-suspended).
     */
    private Instant restoredAt;

    // --- Login & Last seen track ---
    private Instant lastLoginAt;
    private Instant lastSeenAt;

    // --- Relation mapping ---
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

}
