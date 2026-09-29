package com.massseat.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Tenant API এর জন্য কাস্টম Exception
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TenantApiException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public TenantApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public TenantApiException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Invalid search type exception
     */
    public static TenantApiException invalidSearchType(String searchType) {
        return new TenantApiException(
                "অবৈধ সার্চ টাইপ: " + searchType + ". শুধুমাত্র ROOM_WISE বা PROPERTY_WISE অনুমোদিত।",
                "INVALID_SEARCH_TYPE",
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Missing location exception
     */
    public static TenantApiException missingLocation() {
        return new TenantApiException(
                "অন্তত একটি location parameter প্রয়োজন (division, district, বা area)",
                "MISSING_LOCATION",
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Invalid pagination exception
     */
    public static TenantApiException invalidPagination(String message) {
        return new TenantApiException(
                message,
                "INVALID_PAGINATION",
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Property not approved exception
     */
    public static TenantApiException propertyNotApproved(Long propertyId) {
        return new TenantApiException(
                "প্রপার্টি #" + propertyId + " অনুমোদিত নয়",
                "PROPERTY_NOT_APPROVED",
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Room not found exception
     */
    public static TenantApiException roomNotFound(Long roomId) {
        return new TenantApiException(
                "রুম #" + roomId + " পাওয়া যায়নি",
                "ROOM_NOT_FOUND",
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * No results found exception
     */
    public static TenantApiException noResultsFound(String searchType) {
        return new TenantApiException(
                searchType + " সার্চে কোন ফলাফল পাওয়া যায়নি",
                "NO_RESULTS_FOUND",
                HttpStatus.NOT_FOUND
        );
    }
}