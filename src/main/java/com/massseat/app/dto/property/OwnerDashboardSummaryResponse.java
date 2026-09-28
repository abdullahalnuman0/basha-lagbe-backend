package com.massseat.app.dto.property;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OwnerDashboardSummaryResponse {
    private long activeListings;
    private long pendingApproval;
    private long availableSeats;
    private long totalRoom;

}
