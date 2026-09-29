package com.massseat.app.dto.tenant;

import com.massseat.app.entity.enums.PropertyGender;
import com.massseat.app.entity.enums.PropertyType;
import com.massseat.app.entity.enums.RoomType;
import lombok.*;

import java.util.List;

/**
 * Tenant এর জন্য সার্চ এবং ফিল্টার রিকোয়েস্ট
 * এই ক্লাসটি Room এবং Property উভয় টাইপের সার্চে ব্যবহৃত হয়
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSearchCriteria {

    // ============ Location Based Filtering ============
    private String division;
    private String district;
    private String area;

    // ============ Room/Property Type Filtering ============
    /**
     * ROOM_WISE বা PROPERTY_WISE সার্চ করতে হবে?
     * এটা হচ্ছে মেইন ফিল্টার
     */
    private SearchType searchType; // ROOM_WISE, PROPERTY_WISE

    // ============ Room Specific Filters ============
    private RoomType roomType;
    private Integer minRent;
    private Integer maxRent;
    private Integer minAvailableSeats;
    private Integer maxAvailableSeats;
    private Integer minRoomSize; // (width * height) এর ভিত্তিতে

    // ============ Property Specific Filters ============
    private PropertyType propertyType;
    private PropertyGender gender;
    private List<String> facilities;


    // ============ Sorting ============
    private String sortBy = "createdAt"; // createdAt, rent, availableSeats
    private String sortOrder = "desc"; // asc, desc

    /**
     * সার্চ টাইপ - ROOM_WISE বা PROPERTY_WISE
     */
    public enum SearchType {
        ROOM_WISE,      // রুম অনুযায়ী সার্চ করবে
        PROPERTY_WISE   // প্রপার্টি অনুযায়ী সার্চ করবে
    }
}