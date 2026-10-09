package com.massseat.app.service;

import com.massseat.app.entity.User;
import com.massseat.app.exception.BadRequestException;
import com.massseat.app.exception.ResourceNotFoundException;
import com.massseat.app.repository.RefreshTokenRepository;
import com.massseat.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final FcmService fcmService;

    @Transactional(readOnly = true)
    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    /** Stores this device's Firebase Cloud Messaging token for web push. */
    @Transactional
    public void updateFcmToken(Long userId, String token) {
        User user = getUserOrThrow(userId);
        user.setFcmToken(token);
        userRepository.save(user);
    }

    /**
     * In-app presence heartbeat. While the heartbeat is fresh the user gets
     * in-app (WebSocket) notifications only; push is reserved for absent users.
     */
    @Transactional
    public void heartbeat(Long userId) {
        User user = getUserOrThrow(userId);
        user.setLastSeenAt(Instant.now());
        userRepository.save(user);
    }

    @Transactional
    public String sendTestPush(Long userId) {
        User user = getUserOrThrow(userId);
        if (!fcmService.isEnabled()) {
            throw new BadRequestException(
                    "Server-side push is disabled — set FCM_SERVICE_ACCOUNT_FILE (or drop the "
                            + "firebase-adminsdk JSON in the backend folder) and restart the server.");
        }
        if (!org.springframework.util.StringUtils.hasText(user.getFcmToken())) {
            throw new BadRequestException(
                    "No device is registered for push. Allow notifications in the browser first "
                            + "(and open the site via https:// or http://localhost).");
        }
        FcmService.PushResult result = fcmService.sendPushSync(
                user.getFcmToken(),
                "DoramEasy টেস্ট নোটিফিকেশন 🔔",
                "Push notification ঠিকমতো কাজ করছে!",
                java.util.Map.of("type", "TEST", "refType", "NOTIFICATION"));
        return switch (result) {
            case SENT -> "Test push sent — check your device notifications.";
            case INVALID_TOKEN -> throw new BadRequestException(
                    "Your device token was stale and has been cleared. Re-enable notifications and try again.");
            case SKIPPED -> throw new BadRequestException("Push was skipped (no token or push disabled).");
            case ERROR -> throw new BadRequestException(
                    "Firebase rejected the push. Check the server logs and the Firebase project setup.");
        };
    }

}
