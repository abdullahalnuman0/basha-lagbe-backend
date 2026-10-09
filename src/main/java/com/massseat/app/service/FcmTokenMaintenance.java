package com.massseat.app.service;

import com.massseat.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmTokenMaintenance {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void invalidate(String token) {
        try {
            userRepository.clearFcmToken(token);
            log.debug("Cleared stale FCM token");
        } catch (Exception ex) {
            log.warn("Failed to clear stale FCM token: {}", ex.getMessage());
        }
    }
}
