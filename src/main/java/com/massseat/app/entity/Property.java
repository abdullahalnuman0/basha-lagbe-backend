package com.massseat.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.massseat.app.entity.enums.PropertyGender;
import com.massseat.app.entity.enums.PropertyType;
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
@Table(
        name = "properties"
)
public class Property extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // property view by user
    @Builder.Default
    private long totalViews = 0;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    // --- Area ---
    @Column(length = 100, nullable = false)
    private String division;

    @Column(length = 100, nullable = false)
    private String district;

    @Column(length = 150, nullable = false)
    private String area;

    @Column(length = 200, nullable = false)
    private String address;

    private String contactNumber;

    @Builder.Default
    private boolean isApproved = false;

    // --- Rear by ---
    @Column(length = 200)
    private String institutionNearby;

    // -- Enums ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PropertyGender gender;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "property_facilities", joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "facility")
    private List<String> facilities = new ArrayList<>();

    // --- Relations ----
    @Builder.Default
    @OneToMany(mappedBy = "property",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER)
    private List<Room> rooms = new ArrayList<>();

    // --- Helper methods to manage bidirectional relationship ---
    public void addRoom(Room room) {
        room.setProperty(this);
        rooms.add(room);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
        room.setProperty(null);
    }


}


/* Room
    private Integer availableSeats;

    private String roomSize;
 */
