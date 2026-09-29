package com.massseat.app.dto.tenant;

import com.massseat.app.entity.enums.PropertyGender;
import com.massseat.app.entity.enums.PropertyType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Tenant এর জন্য Property সার্চ রেসপন্স
 * এটা Property-wise সার্চে ব্যবহার হয়
 * প্রপার্টির সাথে সেই প্রপার্টির সব রুমের তথ্য দেখায়
 */
@Getter
@Builder
public class PropertyTenantResponse {

    // ============ Owner Information ============
    private Long ownerId;
    private String ownerName;
    private String ownerAvatarUrl;

    // ============ Property Information ============
    private Long propertyId;
    private String title;
    private String description;
    private String division;
    private String district;
    private String area;
    private String address;
    private String contactNumber;
    private String institutionNearby;
    private PropertyType propertyType;
    private PropertyGender gender;
    private List<String> facilities;

    // ============ Room Information ============
    /**
     * এই প্রপার্টির সব রুমের তথ্য
     * (একটি প্রপার্টির মধ্যে একাধিক রুম থাকতে পারে)
     */
    private List<RoomTenantResponse> rooms;

    /**
     * প্রপার্টির মধ্যে মোট রুমের সংখ্যা
     */
    private Integer totalRooms;

    /**
     * প্রপার্টির সব রুমে মোট কত সিট ফাঁকা আছে
     */
    private Integer totalAvailableSeats;

    // ============ Meta Information ============
    private Instant createdAt;
}