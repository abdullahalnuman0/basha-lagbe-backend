package com.massseat.app.service;

import com.massseat.app.dto.tenant.PropertyTenantResponse;
import com.massseat.app.dto.tenant.RoomTenantResponse;
import com.massseat.app.dto.tenant.TenantSearchCriteria;
import com.massseat.app.entity.Property;
import com.massseat.app.entity.Room;
import com.massseat.app.entity.User;
import com.massseat.app.exception.ResourceNotFoundException;
import com.massseat.app.repository.PropertyRepository;
import com.massseat.app.repository.RoomRepository;
import com.massseat.app.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tenant এর জন্য সার্চ এবং ফিল্টার সার্ভিস
 * এখানে Room এবং Property উভয় ধরনের সার্চ করা যায়
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final RoomRepository tenantRepository;
    private final PropertyRepository tenantPropertyRepository;


    @Transactional(readOnly = true)
    public PageResponse<RoomTenantResponse> searchRooms(
            TenantSearchCriteria criteria, Pageable pageable
    ) {
        log.info("ROOM_WISE Search: division={}, district={}, area={}",
                criteria.getDivision(), criteria.getDistrict(), criteria.getArea());

        Page<Room> rooms = tenantRepository.searchRoomsByFilters(
                criteria.getDivision(),
                criteria.getDistrict(),
                criteria.getArea(),
                criteria.getRoomType(),
                criteria.getMinRent(),
                criteria.getMaxRent(),
                criteria.getMinAvailableSeats(),
                criteria.getMaxAvailableSeats(),
                criteria.getPropertyType(),
                criteria.getGender(),
                pageable
        );

        PageResponse<RoomTenantResponse> responses = PageResponse.of(rooms, this::convertRoomToTenantResponse);
        log.info("Total {} Room founded", responses.getTotalElements());

        return responses;
    }

    @Transactional(readOnly = true)
    public PageResponse<PropertyTenantResponse> searchProperties(
            TenantSearchCriteria criteria,
            Pageable pageable) {

        log.info("PROPERTY_WISE Search: division={}, district={}, area={}",
                criteria.getDivision(), criteria.getDistrict(), criteria.getArea());

        Page<Property> properties = tenantPropertyRepository.searchPropertiesByFilters(
                criteria.getDivision(),
                criteria.getDistrict(),
                criteria.getArea(),
                criteria.getPropertyType(),
                criteria.getGender(),
                pageable
        );

        PageResponse<PropertyTenantResponse> responses = PageResponse.of(properties, this::convertPropertyToTenantResponse);

        log.info("Total {} Property founded", responses.getTotalElements());

        return responses;
    }

    // ===============================================
    // find my id
    // ===============================================

    @Transactional(readOnly = true)
    public RoomTenantResponse getRoomDetail(Long roomId) {

        Room room = tenantRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        // Check যে property approved আছে কিনা
        if (!room.getProperty().isApproved()) {
            throw new ResourceNotFoundException("Room", roomId);
        }

        return convertRoomToTenantResponse(room);
    }

    @Transactional(readOnly = true)
    public PropertyTenantResponse getPropertyDetail(Long propertyId) {

        Property property = tenantPropertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        // Check যে property approved আছে কিনা
        if (!property.isApproved()) {
            throw new ResourceNotFoundException("Property", propertyId);
        }

        return convertPropertyToTenantResponse(property);
    }


    // ===============================================
    // Helper methods
    // ===============================================

    /**
     * Room to RoomTenantResponse convert
     */
    private RoomTenantResponse convertRoomToTenantResponse(Room room) {
        Property property = room.getProperty();
        User owner = property.getUser();

        return RoomTenantResponse.builder()
                // Owner info
                .ownerId(owner.getId())
                .ownerName(owner.getFullName())
                .ownerAvatarUrl(owner.getAvatarUrl())
                // Property info
                .propertyId(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .division(property.getDivision())
                .district(property.getDistrict())
                .area(property.getArea())
                .address(property.getAddress())
                .contactNumber(property.getContactNumber())
                .institutionNearby(property.getInstitutionNearby())
                .propertyType(property.getType())
                .gender(property.getGender())
                .facilities(property.getFacilities())
                // room info
                .roomId(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getType())
                .rent(room.getRent())
                .deposit(room.getDeposit())
                .availableSeats(room.getAvailableSeats())
                .roomWidth(room.getRoomWidth())
                .roomHeight(room.getRoomHeight())
                .images(room.getImages())
                .createdAt(room.getCreatedAt())
                .build();
    }

    /**
     * Property to PropertyTenantResponse convert
     */
    private PropertyTenantResponse convertPropertyToTenantResponse(Property property) {
        User owner = property.getUser();

        // collect rooms info
        List<RoomTenantResponse> roomResponses = property.getRooms().stream()
                .map(this::convertRoomToTenantResponse)
                .toList();

        // Total available seats calculate
        int totalAvailableSeats = property.getRooms().stream()
                .mapToInt(Room::getAvailableSeats)
                .sum();

        return PropertyTenantResponse.builder()
                .ownerId(owner.getId())
                .ownerName(owner.getFullName())
                .ownerAvatarUrl(owner.getAvatarUrl())
                .propertyId(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .division(property.getDivision())
                .district(property.getDistrict())
                .area(property.getArea())
                .address(property.getAddress())
                .contactNumber(property.getContactNumber())
                .institutionNearby(property.getInstitutionNearby())
                .propertyType(property.getType())
                .gender(property.getGender())
                .facilities(property.getFacilities())
                .rooms(roomResponses)
                .totalRooms(property.getRooms().size())
                .totalAvailableSeats(totalAvailableSeats)
                .createdAt(property.getCreatedAt())
                .build();
    }
}