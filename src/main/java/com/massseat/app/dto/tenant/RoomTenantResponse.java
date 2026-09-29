package com.massseat.app.dto.tenant;

import com.massseat.app.entity.enums.PropertyGender;
import com.massseat.app.entity.enums.PropertyType;
import com.massseat.app.entity.enums.RoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class RoomTenantResponse {

    private Long ownerId;
    private String ownerName;
    private String ownerAvatarUrl;

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
    //------------------------------------
    private Long roomId;
    private String roomNumber;
    private RoomType roomType;
    private Integer rent;
    private Integer deposit;
    private Integer availableSeats;
    private Integer roomWidth;
    private Integer roomHeight;
    private List<String> images;
    private Instant createdAt;

}
