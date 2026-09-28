package com.massseat.app.scheduler;

import com.massseat.app.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyApprovalScheduler {

    private final PropertyRepository propertyRepository;

    private static final long APPROVAL_HOURS = 1;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void approvePendingProperties() {

        Instant cutoff = Instant.now()
                .minus(APPROVAL_HOURS, ChronoUnit.HOURS);

        int approvedCount =
                propertyRepository.approveExpiredProperties(cutoff);

        if (approvedCount > 0) {
            log.info("Automatically approved {} properties", approvedCount);
        }
    }
}
