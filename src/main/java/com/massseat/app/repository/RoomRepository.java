package com.massseat.app.repository;

import com.massseat.app.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface RoomRepository extends JpaRepository<Room, Long> {

    long countByPropertyUserId(Long userId);

//    ====================================

    /**
     * Tenant এর জন্য Room সার্চ এবং ফিল্টার Repository
     * এখানে শুধুমাত্র APPROVED properties এর rooms দেখানো হবে
     */

//    ====================================
    @Query("""
            SELECT COALESCE(SUM(r.availableSeats), 0)
            FROM Room r
            WHERE r.property.user.id = :userId
            """)
    long sumAvailableSeatsByUserId(@Param("userId") Long userId);


    /**
     * Location এবং অন্যান্য filters এর ভিত্তিতে Room খোঁজা
     * শুধুমাত্র APPROVED properties থেকে rooms দেখাবে
     */
    @Query("""
            SELECT r FROM Room r
            JOIN r.property p
            WHERE p.isApproved = true
            AND (:division IS NULL OR p.division = :division)
            AND (:district IS NULL OR p.district = :district)
            AND (:area IS NULL OR p.area = :area)
            AND (:roomType IS NULL OR r.type = :roomType)
            AND (:minRent IS NULL OR r.rent >= :minRent)
            AND (:maxRent IS NULL OR r.rent <= :maxRent)
            AND (:minSeats IS NULL OR r.availableSeats >= :minSeats)
            AND (:maxSeats IS NULL OR r.availableSeats <= :maxSeats)
            AND (:propertyType IS NULL OR p.type = :propertyType)
            AND (:gender IS NULL OR p.gender = :gender)
            """)
    Page<Room> searchRoomsByFilters(
            @Param("division") String division,
            @Param("district") String district,
            @Param("area") String area,
            @Param("roomType") Object roomType,
            @Param("minRent") Integer minRent,
            @Param("maxRent") Integer maxRent,
            @Param("minSeats") Integer minSeats,
            @Param("maxSeats") Integer maxSeats,
            @Param("propertyType") Object propertyType,
            @Param("gender") Object gender,
            Pageable pageable
    );

    @Query(
            value = """
                SELECT r
                FROM Room r
                JOIN r.property p
                WHERE p.isApproved = true

                AND (
                    :keyword IS NULL
                    OR :keyword = ''
                    OR LOWER(COALESCE(p.title, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(p.address, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(p.institutionNearby, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(p.area, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )

                AND (:division IS NULL OR p.division = :division)
                AND (:district IS NULL OR p.district = :district)
                AND (:area IS NULL OR p.area = :area)

                AND (:roomType IS NULL OR r.type = :roomType)
                AND (:minRent IS NULL OR r.rent >= :minRent)
                AND (:maxRent IS NULL OR r.rent <= :maxRent)
                AND (:minSeats IS NULL OR r.availableSeats >= :minSeats)
                AND (:maxSeats IS NULL OR r.availableSeats <= :maxSeats)

                AND (
                    :minRoomSize IS NULL
                    OR (r.roomWidth * r.roomHeight) >= :minRoomSize
                )

                AND (:propertyType IS NULL OR p.type = :propertyType)
                AND (:gender IS NULL OR p.gender = :gender)

                ORDER BY
                    CASE
                        WHEN :keyword IS NULL OR :keyword = '' THEN 999

                        WHEN LOWER(COALESCE(p.title, ''))
                             LIKE LOWER(CONCAT('%', :keyword, '%'))
                            THEN 1

                        WHEN LOWER(COALESCE(p.address, ''))
                             LIKE LOWER(CONCAT('%', :keyword, '%'))
                            THEN 2

                        WHEN LOWER(COALESCE(p.institutionNearby, ''))
                             LIKE LOWER(CONCAT('%', :keyword, '%'))
                            THEN 3

                        WHEN LOWER(COALESCE(p.area, ''))
                             LIKE LOWER(CONCAT('%', :keyword, '%'))
                            THEN 4

                        ELSE 999
                    END
                """,
            countQuery = """
                SELECT COUNT(r)
                FROM Room r
                JOIN r.property p
                WHERE p.isApproved = true

                AND (
                    :keyword IS NULL
                    OR :keyword = ''
                    OR LOWER(COALESCE(p.title, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(p.address, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(p.institutionNearby, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(COALESCE(p.area, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                )

                AND (:division IS NULL OR p.division = :division)
                AND (:district IS NULL OR p.district = :district)
                AND (:area IS NULL OR p.area = :area)

                AND (:roomType IS NULL OR r.type = :roomType)
                AND (:minRent IS NULL OR r.rent >= :minRent)
                AND (:maxRent IS NULL OR r.rent <= :maxRent)
                AND (:minSeats IS NULL OR r.availableSeats >= :minSeats)
                AND (:maxSeats IS NULL OR r.availableSeats <= :maxSeats)

                AND (
                    :minRoomSize IS NULL
                    OR (r.roomWidth * r.roomHeight) >= :minRoomSize
                )

                AND (:propertyType IS NULL OR p.type = :propertyType)
                AND (:gender IS NULL OR p.gender = :gender)
                """
    )
    Page<Room> searchRoomsByFilters(
            @Param("keyword") String keyword,
            @Param("division") String division,
            @Param("district") String district,
            @Param("area") String area,
            @Param("roomType") Object roomType,
            @Param("minRent") Integer minRent,
            @Param("maxRent") Integer maxRent,
            @Param("minSeats") Integer minSeats,
            @Param("maxSeats") Integer maxSeats,
            @Param("minRoomSize") Integer minRoomSize,
            @Param("propertyType") Object propertyType,
            @Param("gender") Object gender,
            Pageable pageable
    );

    @Modifying
    @Query("""
        UPDATE Room r
        SET r.totalViews = r.totalViews + 1
        WHERE r.id = :roomId
    """)
    int incrementTotalViews(@Param("roomId") long roomId);
}