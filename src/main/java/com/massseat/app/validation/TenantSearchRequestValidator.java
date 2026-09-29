package com.massseat.app.validation;

import com.massseat.app.dto.tenant.TenantSearchCriteria;
import org.springframework.stereotype.Component;

/**
 * TenantSearchRequest ভ্যালিডেশন হেল্পার
 */
//@Component
public class TenantSearchRequestValidator {

    /**
     * সম্পূর্ণ সার্চ রিকোয়েস্ট ভ্যালিডেট করা
     */
    public ValidationResult validate(TenantSearchCriteria request) {
        ValidationResult result = new ValidationResult();

        if (request == null) {
            result.addError("request", "রিকোয়েস্ট বডি প্রয়োজন");
            return result;
        }

        // SearchType ভ্যালিডেশন
        if (request.getSearchType() != null) {
            try {
                TenantSearchCriteria.SearchType.valueOf(request.getSearchType().toString());
            } catch (IllegalArgumentException e) {
                result.addError("searchType", "অবৈধ সার্চ টাইপ: " + request.getSearchType());
            }
        }

        // Rent ভ্যালিডেশন
        if (request.getMinRent() != null && request.getMinRent() < 0) {
            result.addError("minRent", "ন্যূনতম ভাড়া নেগেটিভ হতে পারে না");
        }

        if (request.getMaxRent() != null && request.getMaxRent() < 0) {
            result.addError("maxRent", "সর্বোচ্চ ভাড়া নেগেটিভ হতে পারে না");
        }

        if (request.getMinRent() != null && request.getMaxRent() != null) {
            if (request.getMinRent() > request.getMaxRent()) {
                result.addError("rent", "ন্যূনতম ভাড়া সর্বোচ্চ ভাড়ার চেয়ে বেশি হতে পারে না");
            }
        }

        // Available Seats ভ্যালিডেশন
        if (request.getMinAvailableSeats() != null && request.getMinAvailableSeats() < 0) {
            result.addError("minAvailableSeats", "ন্যূনতম সিট নেগেটিভ হতে পারে না");
        }

        if (request.getMaxAvailableSeats() != null && request.getMaxAvailableSeats() < 0) {
            result.addError("maxAvailableSeats", "সর্বোচ্চ সিট নেগেটিভ হতে পারে না");
        }

        // Room Size ভ্যালিডেশন
        if (request.getMinRoomSize() != null && request.getMinRoomSize() < 0) {
            result.addError("minRoomSize", "রুমের আকার নেগেটিভ হতে পারে না");
        }

        // SortBy ভ্যালিডেশন
        if (request.getSortBy() != null) {
            String sortBy = request.getSortBy().toLowerCase();
            if (!sortBy.equals("createdat") &&
                    !sortBy.equals("rent") &&
                    !sortBy.equals("availableseats")) {
                result.addError("sortBy", "অবৈধ সর্টিং অপশন। ব্যবহার করুন: createdAt, rent, availableSeats");
            }
        }

        // SortOrder ভ্যালিডেশন
        if (request.getSortOrder() != null) {
            String sortOrder = request.getSortOrder().toLowerCase();
            if (!sortOrder.equals("asc") && !sortOrder.equals("desc")) {
                result.addError("sortOrder", "অবৈধ সর্টিং ক্রম। ব্যবহার করুন: asc, desc");
            }
        }

        return result;
    }

    /**
     * শুধুমাত্র ROOM_WISE সার্চ ভ্যালিডেশন
     */
    public ValidationResult validateRoomWiseSearch(TenantSearchCriteria request) {
        ValidationResult result = validate(request);

        // ROOM_WISE specific validations
        if (request.getMinRoomSize() != null && request.getMinRoomSize() < 0) {
            result.addError("minRoomSize", "রুমের আকার নেগেটিভ হতে পারে না");
        }

        return result;
    }

    /**
     * শুধুমাত্র PROPERTY_WISE সার্চ ভ্যালিডেশন
     */
    public ValidationResult validatePropertyWiseSearch(TenantSearchCriteria request) {
        ValidationResult result = validate(request);

        // PROPERTY_WISE specific validations
        // (বর্তমানে কোন নির্দিষ্ট validation নেই)

        return result;
    }

    /**
     * হোমপেজ রিকোয়েস্ট ভ্যালিডেশন
     */
    public ValidationResult validateNearbyRequest(String division, String district) {
        ValidationResult result = new ValidationResult();

        if (division == null || division.trim().isEmpty()) {
            result.addError("division", "Division প্রয়োজন");
        }

        if (district == null || district.trim().isEmpty()) {
            result.addError("district", "District প্রয়োজন");
        }

        return result;
    }

    /**
     * ভ্যালিডেশন রেজাল্ট ক্লাস
     */
    public static class ValidationResult {
        private final java.util.Map<String, String> errors = new java.util.HashMap<>();

        public void addError(String field, String message) {
            errors.put(field, message);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public java.util.Map<String, String> getErrors() {
            return errors;
        }

        public String getFirstError() {
            return errors.isEmpty() ? null : errors.values().iterator().next();
        }
    }
}