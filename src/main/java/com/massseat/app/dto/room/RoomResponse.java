package com.massseat.app.dto.room;

import com.massseat.app.entity.Room;
import com.massseat.app.entity.enums.RoomType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class RoomResponse {

    private long id;
    private String roomNumber;
    private RoomType type;
    private int rent;
    private int deposit;
    private int availableSeats;
    private int roomWidth;
    private int roomHeight;
    private List<String> images;
    private Instant createdAt;

    //----------------------------------
    public static RoomResponse from(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .type(room.getType())
                .rent(room.getRent())
                .deposit(room.getDeposit())
                .availableSeats(room.getAvailableSeats())
                .roomWidth(room.getRoomWidth())
                .roomHeight(room.getRoomHeight())
                .images(room.getImages())
                .createdAt(room.getCreatedAt())
                .build();
    }

    public static List<RoomResponse> from(List<Room> rooms) {
        return rooms.stream().map(RoomResponse::from)
                .toList();
    }

}
