package com.massseat.app.dto.property;

import com.massseat.app.dto.room.RoomRequest;
import com.massseat.app.entity.enums.PropertyGender;
import com.massseat.app.entity.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class PropertyUpdateRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    private String description;

    @NotBlank
    @Size(max = 100)
    private String division;

    @NotBlank
    @Size(max = 100)
    private String district;

    @NotBlank
    @Size(max = 150)
    private String area;

    @NotBlank
    @Size(max = 200)
    private String address;

    private String contactNumber;

    @Size(max = 200)
    private String institutionNearby;

    private PropertyType type;
    private PropertyGender gender;

}
