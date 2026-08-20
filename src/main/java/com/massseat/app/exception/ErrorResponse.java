package com.massseat.app.exception;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String message;
    private String error;
    private String path;
    @Builder.Default
    private Instant timestamp = Instant.now();
    private Map<String, String> fieldErrors;

    /**
     * Returns a reason code when the client needs to handle a specific case,
     * such as an account being pending deletion{@code ACCOUNT_PENDING_DELETION}, suspended{@code ACCOUNT_SUSPENDED}, or banned{@code ACCOUNT_BANNED}.
     * Returns null when there is no specific reason.
     */
    private String code;

}
