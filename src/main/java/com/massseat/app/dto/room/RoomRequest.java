package com.massseat.app.dto.room;

import com.massseat.app.entity.enums.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class RoomRequest {

    private String roomNumber;

    private RoomType type;

    @Min(0)
    private int rent;

    @Min(0)
    private int deposit;

    @NotNull
    @Min(1)
    private Integer availableSeats;

    @NotNull
    @Min(1)
    private Integer roomWidth;

    @NotNull
    @Min(1)
    private Integer roomHeight;

    @NotNull
    @Size(min = 1)
    private List<String> images;

}
