package com.massseat.app.entity;

import com.massseat.app.entity.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "top_tokens",
        indexes = {
                @Index(name = "idx_otp_email",columnList = "email")
        }
)
public class OtpToken extends BaseEntity {

    @Column(nullable = false,length = 150)
    private String email;

    @Column(nullable = false,length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private OtpPurpose purpose;

    @Column(nullable = false)
    private Instant expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    @Builder.Default
    @Column(nullable = false)
    private int attempts = 0;

}
