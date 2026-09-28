package com.massseat.app.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.massseat.app.entity.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Room extends BaseEntity {

    // property view by user
    @Builder.Default
    private long totalViews = 0;

    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Column(nullable = false)
    private int rent;

    private int deposit;

    @Column(nullable = false)
    private int availableSeats;

    // --- Room size (Fit) ---
    @Column(nullable = false)
    private int roomWidth;

    @Column(nullable = false)
    private int roomHeight;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url", length = 500)
    private List<String> images = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;


    // --- Helper method ---
    public void addImage(String image) {
        images.add(image);
    }
    public void removeImage(String image) {
        images.remove(image);
    }

}
/*
@Column(nullable = false)
private int totalSeats;
*/