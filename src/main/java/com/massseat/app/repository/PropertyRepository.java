package com.massseat.app.repository;

import com.massseat.app.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    Page<Property> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Property> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Property> findByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    long countByUserIdAndIsApprovedTrue(Long userId);

    long countByUserIdAndIsApprovedFalse(Long userId);

    @Modifying
    @Query("""
            UPDATE Property p
            SET p.isApproved = true
            WHERE p.isApproved = false
            AND p.createdAt < :cutoff
            """)
    int approveExpiredProperties(@Param("cutoff") Instant cutoff);

//=======================================================
/**
 * Tenant এর জন্য Property সার্চ এবং ফিল্টার Repository
 * এখানে শুধুমাত্র APPROVED properties দেখানো হবে
 */
//=======================================================

    /**
     * Location এবং অন্যান্য filters এর ভিত্তিতে Property খোঁজা
     * শুধুমাত্র APPROVED properties থেকে দেখাবে
     */
    @Query("""
            SELECT p FROM Property p
            WHERE p.isApproved = true
            AND (:division IS NULL OR p.division = :division)
            AND (:district IS NULL OR p.district = :district)
            AND (:area IS NULL OR p.area = :area)
            AND (:propertyType IS NULL OR p.type = :propertyType)
            AND (:gender IS NULL OR p.gender = :gender)
            ORDER BY p.createdAt DESC
            """)
    Page<Property> searchPropertiesByFilters(
            @Param("division") String division,
            @Param("district") String district,
            @Param("area") String area,
            @Param("propertyType") Object propertyType,
            @Param("gender") Object gender,
            Pageable pageable
    );

    /**
     * ইউজারের লোকেশনের কাছাকাছি প্রপার্টি খোঁজা (হোমপেজের জন্য)
     */
    @Query("""
            SELECT p FROM Property p
            WHERE p.isApproved = true
            AND p.division = :division
            AND p.district = :district
            ORDER BY p.createdAt DESC
            """)
    Page<Property> findNearbyPropertiesByLocation(
            @Param("division") String division,
            @Param("district") String district,
            Pageable pageable
    );

    /**
     * Title এবং ডেসক্রিপশন অনুযায়ী Property সার্চ
     */
    @Query("""
            SELECT p FROM Property p
            WHERE p.isApproved = true
            AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:division IS NULL OR p.division = :division)
            AND (:district IS NULL OR p.district = :district)
            ORDER BY p.createdAt DESC
            """)
    Page<Property> searchPropertiesByKeyword(
            @Param("keyword") String keyword,
            @Param("division") String division,
            @Param("district") String district,
            Pageable pageable
    );

    /**
     * সর্বোচ্চ available seats আছে এমন properties খোঁজা
     */
    @Query("""
            SELECT p FROM Property p
            WHERE p.isApproved = true
            AND (:division IS NULL OR p.division = :division)
            AND (:district IS NULL OR p.district = :district)
            AND (SELECT COALESCE(SUM(r.availableSeats), 0) FROM Room r WHERE r.property = p) >= :minSeats
            ORDER BY p.createdAt DESC
            """)
    Page<Property> searchPropertiesByAvailableSeats(
            @Param("division") String division,
            @Param("district") String district,
            @Param("minSeats") Integer minSeats,
            Pageable pageable
    );
}
