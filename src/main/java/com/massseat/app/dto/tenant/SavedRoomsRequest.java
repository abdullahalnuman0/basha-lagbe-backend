package com.massseat.app.dto.tenant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SavedRoomsRequest(

        @NotNull(message = "Room IDs must not be null")
        @Size(max = 100, message = "Maximum 100 room IDs are allowed")
        List<@NotNull @Positive Long> roomIds

) {
}