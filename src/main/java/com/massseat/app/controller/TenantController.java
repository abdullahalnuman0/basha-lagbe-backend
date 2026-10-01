package com.massseat.app.controller;

import com.massseat.app.dto.tenant.PropertyTenantResponse;
import com.massseat.app.dto.tenant.RoomTenantResponse;
import com.massseat.app.dto.tenant.TenantSearchCriteria;
import com.massseat.app.service.TenantService;
import com.massseat.app.utils.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Tenant (ইউজার) সাইডের কন্ট্রোলার
 * এখানে রুম এবং প্রপার্টি খোঁজার সকল এপিআই থাকবে
 */
@RestController
@RequestMapping("/api/v1/tenant")
@RequiredArgsConstructor
@Tag(
        name = "01. Tenant (User)",
        description = "ইউজারদের জন্য - রুম এবং প্রপার্টি খোঁজা, সার্চ এবং ফিল্টার"
)
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/rooms")
    public PageResponse<RoomTenantResponse> searchRooms(
            @ParameterObject TenantSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        return tenantService.searchRooms(criteria, pageable);
    }

    @GetMapping("/properties")
    public PageResponse<PropertyTenantResponse> searchProperties(
            @ParameterObject TenantSearchCriteria criteria,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        return tenantService.searchProperties(criteria, pageable);
    }


    // =============================================
    // ইন্ডিভিজুয়াল রুম/প্রপার্টি ডিটেইল
    // =============================================

    /**
     * একটি নির্দিষ্ট রুমের বিস্তারিত তথ্য
     * <p>
     * Example: /api/v1/tenant/rooms/5
     */
    @GetMapping("/rooms/{roomId}")
    @Operation(
            summary = "রুমের বিস্তারিত তথ্য",
            description = "একটি নির্দিষ্ট রুমের সমস্ত তথ্য (যেমন- ছবি, ভাড়া, উপলব্ধ সিট ইত্যাদি)"
    )
    public ResponseEntity<RoomTenantResponse> getRoomDetail(
            @Parameter(description = "রুমের আইডি")
            @PathVariable Long roomId
    ) {
        RoomTenantResponse response = tenantService.getRoomDetail(roomId);
        return ResponseEntity.ok(response);
    }

    /**
     * একটি নির্দিষ্ট প্রপার্টির বিস্তারিত তথ্য
     * <p>
     * Example: /api/v1/tenant/properties/3
     */
    @GetMapping("/properties/{propertyId}")
    @Operation(
            summary = "প্রপার্টির বিস্তারিত তথ্য",
            description = "একটি নির্দিষ্ট প্রপার্টির সমস্ত তথ্য এবং সেই প্রপার্টির সব রুমের তথ্য"
    )
    public ResponseEntity<PropertyTenantResponse> getPropertyDetail(
            @Parameter(description = "প্রপার্টির আইডি")
            @PathVariable Long propertyId
    ) {
        PropertyTenantResponse response = tenantService.getPropertyDetail(propertyId);
        return ResponseEntity.ok(response);
    }


    //================================================
    // Count methods
    //================================================
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<Void> updateRoomView(@PathVariable Long roomId) {
        tenantService.updateRoomView(roomId);
        return ResponseEntity.ok().build();
    }
}