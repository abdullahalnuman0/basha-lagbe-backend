package com.massseat.app.dto.room;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RoomAvailabilityRequest {

    @NotNull
    @Min(1)
    private Long roomId;

    @NotNull
    @Min(0)
    private Integer availableSeats;


}
