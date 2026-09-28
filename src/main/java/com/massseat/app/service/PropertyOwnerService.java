package com.massseat.app.service;

import com.massseat.app.dto.property.OwnerDashboardSummaryResponse;
import com.massseat.app.dto.property.PropertyOwnerRequest;
import com.massseat.app.dto.property.PropertyOwnerResponse;
import com.massseat.app.dto.property.PropertyUpdateRequest;
import com.massseat.app.dto.room.RoomAvailabilityRequest;
import com.massseat.app.dto.room.RoomRequest;
import com.massseat.app.dto.room.RoomResponse;
import com.massseat.app.entity.Property;
import com.massseat.app.entity.Room;
import com.massseat.app.entity.User;
import com.massseat.app.exception.ResourceNotFoundException;
import com.massseat.app.repository.PropertyRepository;
import com.massseat.app.repository.RoomRepository;
import com.massseat.app.repository.UserRepository;
import com.massseat.app.security.UserPrincipal;
import com.massseat.app.utils.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyOwnerService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final FileStorageService fileStorageService;

    public PropertyOwnerResponse create(long userId, PropertyOwnerRequest req) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));

        Property post = Property.builder()
                .user(owner)
                .title(req.getTitle())
                .description(req.getDescription())
                .division(req.getDivision())
                .district(req.getDistrict())
                .area(req.getArea())
                .address(req.getAddress())
                .contactNumber(req.getContactNumber())
                .institutionNearby(req.getInstitutionNearby())
                .type(req.getType())
                .gender(req.getGender())
                .facilities(req.getFacilities())
                .build();

        List<Room> rooms = req.getRooms().stream()
                .map(r -> Room.builder()
                        .roomNumber(r.getRoomNumber())
                        .type(r.getType())
                        .rent(r.getRent())
                        .deposit(r.getDeposit())
                        .availableSeats(r.getAvailableSeats())
                        .roomWidth(r.getRoomWidth())
                        .roomHeight(r.getRoomHeight())
                        .images(r.getImages())
                        .property(post)
                        .build())
                .toList();
        post.setRooms(rooms);

        propertyRepository.save(post);

        // remove image form temp file track
        fileStorageService.removeFromTrack(
                post.getRooms().stream()
                        .flatMap(room -> room.getImages().stream())
                        .toList()
        );

        return PropertyOwnerResponse.from(post);
    }

    public PropertyOwnerResponse update(Long userId, Long propertyId, PropertyUpdateRequest request) {

        Property property = propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setDivision(request.getDivision());
        property.setDistrict(request.getDistrict());
        property.setArea(request.getArea());
        property.setAddress(request.getAddress());
        property.setContactNumber(request.getContactNumber());
        property.setInstitutionNearby(request.getInstitutionNearby());
        property.setType(request.getType());
        property.setGender(request.getGender());

        propertyRepository.saveAndFlush(property);

        return PropertyOwnerResponse.fromWithOutRoom(property);
    }

    @Transactional(readOnly = true)
    public PageResponse<PropertyOwnerResponse> findAll(Long id, Pageable pageable) {
        Page<Property> pages = propertyRepository.findAllByUserIdOrderByCreatedAtDesc(id, pageable);
        return PageResponse.of(pages, PropertyOwnerResponse::from);
    }


    @Transactional(readOnly = true)
    public List<PropertyOwnerResponse> findAll(Long id) {
        return propertyRepository.findAllByUserIdOrderByCreatedAtDesc(id)
                .stream().map(PropertyOwnerResponse::from)
                .toList();
    }


    @Transactional(readOnly = true)
    public PropertyOwnerResponse findOne(Long userId, Long postId) {
        Property property = propertyRepository.findByIdAndUserId(postId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", postId));
        return PropertyOwnerResponse.from(property);
    }


    @Transactional
    public void deleteProperty(Long userId, Long postId) {
//        propertyRepository.deleteByIdAndUserId(postId, userId);
        Property property = propertyRepository.findByIdAndUserId(postId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", postId));

        fileStorageService.deleteAllFileFromR2(
                property.getRooms().stream()
                        .flatMap(room -> room.getImages().stream())
                        .toList()
        );

        propertyRepository.delete(property);
    }

    //==========================================
    //    Owner Dashboard
    //==========================================
    @Transactional(readOnly = true)
    public OwnerDashboardSummaryResponse getDashboardSummary(Long userId) {

        long activeListings =
                propertyRepository.countByUserIdAndIsApprovedTrue(userId);

        long pendingApproval =
                propertyRepository.countByUserIdAndIsApprovedFalse(userId);

        long availableSeats =
                roomRepository.sumAvailableSeatsByUserId(userId);

        long totalRooms =
                roomRepository.countByPropertyUserId(userId);

        return OwnerDashboardSummaryResponse.builder()
                .activeListings(activeListings)
                .pendingApproval(pendingApproval)
                .availableSeats(availableSeats)
                .totalRoom(totalRooms)
                .build();
    }


//==========================================
//    Room
//==========================================

    @Transactional
    public RoomResponse createRoom(Long userId, Long propertyId, RoomRequest req) {
        Property property = propertyRepository.findByIdAndUserId(propertyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        Room room = Room.builder()
                .roomNumber(req.getRoomNumber())
                .type(req.getType())
                .rent(req.getRent())
                .deposit(req.getDeposit())
                .availableSeats(req.getAvailableSeats())
                .roomWidth(req.getRoomWidth())
                .roomHeight(req.getRoomHeight())
                .images(req.getImages())
                .property(property)
                .build();

        // Bidirectional relationship maintain
        property.addRoom(room);

        // Explicitly persist Room
        roomRepository.saveAndFlush(room);

        // remove image for track
        fileStorageService.removeFromTrack(room.getImages());

        return RoomResponse.from(room);
    }

    @Transactional
    public RoomResponse updateRoom(Long roomId, RoomRequest req) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        room.setRoomNumber(req.getRoomNumber());
        room.setType(req.getType());
        room.setRent(req.getRent());
        room.setDeposit(req.getDeposit());
        room.setAvailableSeats(req.getAvailableSeats());
        room.setRoomWidth(req.getRoomWidth());
        room.setRoomHeight(req.getRoomHeight());
        room.setImages(req.getImages());

        roomRepository.save(room);

        // remove image for track
        fileStorageService.removeFromTrack(room.getImages());

        return RoomResponse.from(room);
    }


    @Transactional
    public void roomAvailability(RoomAvailabilityRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", request.getRoomId()));
        room.setAvailableSeats(request.getAvailableSeats());
        roomRepository.save(room);
    }


}
