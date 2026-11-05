package com.massseat.app.scheduler;

import com.massseat.app.entity.Property;
import com.massseat.app.repository.PropertyRepository;
import com.massseat.app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyApprovalScheduler {

    private final PropertyRepository propertyRepository;
    private final NotificationService notificationService;

    private static final long APPROVAL_HOURS = 1;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void approvePendingProperties() {

        Instant cutoff = Instant.now()
                .minus(APPROVAL_HOURS, ChronoUnit.HOURS);
        // Step 1: Find pending properties older than cutoff
        List<Property> properties =
                propertyRepository
                        .findAllByIsApprovedFalseAndCreatedAtBefore(cutoff);

        if (properties.isEmpty()) {
            log.debug("No pending properties found for approval.");
            return;
        }

        // Step 2: Approve properties
        List<Property> list = properties.stream()
                .peek(p -> p.setApproved(true))
                .toList();

        // Step 3: Persist approval changes
        propertyRepository.saveAll(list);

        log.info(
                "Automatically approved {} properties",
                properties.size()
        );

        // Step 4: Notify owners
        notificationService.pendingPostApproval(list);

//        int approvedCount = propertyRepository.approveExpiredProperties(cutoff);

    }
}
