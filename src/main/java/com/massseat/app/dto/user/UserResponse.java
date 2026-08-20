package com.massseat.app.dto.user;

import com.massseat.app.entity.User;
import com.massseat.app.entity.enums.Gender;
import com.massseat.app.entity.enums.Role;
import com.massseat.app.entity.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Set<Role> roles;
    private Gender gender;
    private UserStatus status;
    private boolean emailVerified;
    private String division;
    private String district;
    private String area;
    private String institution;
    private String avatarUrl;
    private Instant createdAt;
    /**
     * When a temporary suspension lifts (null = permanent while SUSPENDED).
     */
    private Instant suspensionEndAt;
    private String suspensionReason;
    /**
     * When a pending-deletion account is permanently purged.
     */
    private Instant deletionScheduledAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .gender(user.getGender())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .division(user.getDivision())
                .district(user.getDistrict())
                .area(user.getArea())
                .institution(user.getInstitution())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .suspensionEndAt(user.getSuspensionEndAt())
                .suspensionReason(user.getSuspensionReason())
                .deletionScheduledAt(user.getDeletionScheduledAt())
                .build();
    }

}
