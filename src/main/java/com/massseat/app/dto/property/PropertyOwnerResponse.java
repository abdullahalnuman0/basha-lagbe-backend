package com.massseat.app.dto.property;

import com.massseat.app.dto.room.RoomResponse;
import com.massseat.app.entity.Property;
import com.massseat.app.entity.Room;
import com.massseat.app.entity.enums.PropertyGender;
import com.massseat.app.entity.enums.PropertyType;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class PropertyOwnerResponse {

    private long id;
    private String title;
    private String description;
    private String division;
    private String district;
    private String area;
    private String address;
    private String contactNumber;
    private boolean isApproved;
    private String institutionNearby;
    private PropertyType type;
    private PropertyGender gender;
    private List<String> facilities;
    private List<RoomResponse> rooms;
    private Instant createdAt;


    // ===============================
    public static PropertyOwnerResponse from(Property p) {
        return PropertyOwnerResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .division(p.getDivision())
                .district(p.getDistrict())
                .area(p.getArea())
                .address(p.getAddress())
                .contactNumber(p.getContactNumber())
                .isApproved(p.isApproved())
                .institutionNearby(p.getInstitutionNearby())
                .type(p.getType())
                .gender(p.getGender())
                .facilities(p.getFacilities())
                .rooms(RoomResponse.from(p.getRooms()))
                .createdAt(p.getCreatedAt())
                .build();
    }

    /// Without rooms
    public static PropertyOwnerResponse fromWithOutRoom(Property p) {
        return PropertyOwnerResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .division(p.getDivision())
                .district(p.getDistrict())
                .area(p.getArea())
                .address(p.getAddress())
                .contactNumber(p.getContactNumber())
                .isApproved(p.isApproved())
                .institutionNearby(p.getInstitutionNearby())
                .type(p.getType())
                .gender(p.getGender())
                .facilities(p.getFacilities())
                .rooms(List.of())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
