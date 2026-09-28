package com.massseat.app.controller;

import com.massseat.app.dto.property.OwnerDashboardSummaryResponse;
import com.massseat.app.dto.property.PropertyOwnerRequest;
import com.massseat.app.dto.property.PropertyOwnerResponse;
import com.massseat.app.dto.property.PropertyUpdateRequest;
import com.massseat.app.dto.room.RoomAvailabilityRequest;
import com.massseat.app.dto.room.RoomRequest;
import com.massseat.app.dto.room.RoomResponse;
import com.massseat.app.security.UserPrincipal;
import com.massseat.app.service.PropertyOwnerService;
import com.massseat.app.utils.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/properties")
@RequiredArgsConstructor
@Tag(
        name = "02. Property owner",
        description = "Property add / update / remove"
)
public class PropertyOwnerController {

    private final PropertyOwnerService propertyOwnerService;

    @PostMapping
    public ResponseEntity<PropertyOwnerResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PropertyOwnerRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(propertyOwnerService.create(principal.getId(), request));
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<PropertyOwnerResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long propertyId,
            @Valid @RequestBody PropertyUpdateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(propertyOwnerService.update(principal.getId(), propertyId, request));
    }

    @GetMapping
    public PageResponse<PropertyOwnerResponse> findAll(
            @AuthenticationPrincipal UserPrincipal principal,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        return propertyOwnerService.findAll(principal.getId(), pageable);
    }

    @GetMapping("/all")
    public List<PropertyOwnerResponse> findAll(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return propertyOwnerService.findAll(principal.getId());
    }

    @GetMapping("/{postId}")
    public PropertyOwnerResponse findOne(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId) {
        return propertyOwnerService.findOne(principal.getId(), postId);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId) {

        propertyOwnerService.deleteProperty(principal.getId(), postId);

        return ResponseEntity.noContent().build();
    }

    //==========================================
    //    Owner Dashboard
    //==========================================
    @GetMapping("/dashboard/summary")
    public OwnerDashboardSummaryResponse getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return propertyOwnerService.getDashboardSummary(principal.getId());
    }

//============================================
//    Room
//============================================

    @PostMapping("/room/{propertyId}")
    public ResponseEntity<RoomResponse> createRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long propertyId,
            @Valid @RequestBody RoomRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(propertyOwnerService.createRoom(principal.getId(), propertyId, request));
    }

    @PutMapping("/room/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody RoomRequest request
    ) {
        return ResponseEntity.ok(propertyOwnerService.updateRoom(roomId, request));
    }


    @PutMapping("/room/availability")
    public ResponseEntity<Void> roomAvailability(
            @Valid @RequestBody RoomAvailabilityRequest request
    ) {
        propertyOwnerService.roomAvailability(request);
        return ResponseEntity.ok().build();
    }


}
