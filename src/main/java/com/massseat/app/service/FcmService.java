package com.massseat.app.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.massseat.app.config.AppProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class FcmService {

    public enum PushResult {SENT, SKIPPED, INVALID_TOKEN, ERROR}

    private static final List<String> CONVENTIONAL_NAMES =
            List.of("firebase-service-account.json", "dorameasy-firebase-adminsdk-fbsvc-e6ae53151c.json");

    private final AppProperties appProperties;
    private final FcmTokenMaintenance tokenMaintenance;

    private volatile boolean enabled;

    @PostConstruct
    public void init() {
        try {
            Optional<Path> credentials = resolveCredentialsFile();
            if (credentials.isPresent()) {
                if (FirebaseApp.getApps().isEmpty()) {
                    try (InputStream in = new FileInputStream(credentials.get().toFile())) {
                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(in))
                                .build();
                        log.info("Firebase Option: {}", options);
                        FirebaseApp.initializeApp(options);
                    }
                }
                enabled = true;
                log.info("FcmService: Firebase push ENABLED (credentials: {})",
                        credentials.get().toAbsolutePath());
            } else {
                enabled = false;
                log.warn("FcmService: push DISABLED — no service-account JSON found. "
                        + "Set FCM_SERVICE_ACCOUNT_FILE, or drop a *firebase-adminsdk*.json in the app directory.");
            }
        } catch (Exception ex) {
            enabled = false;
            log.error("FcmService: failed to initialize Firebase Admin SDK: {}", ex.getMessage(), ex);
        }
    }

    @Async
    public void sendPush(String deviceToken, String title, String body, Map<String, String> data) {
        deliver(deviceToken, title, body, data);
    }

    public PushResult sendPushSync(String deviceToken, String title, String body, Map<String, String> data) {
        return deliver(deviceToken, title, body, data);
    }

    private PushResult deliver(String deviceToken, String title, String body, Map<String, String> data) {
        if (!enabled) {
            log.debug("FcmService disabled — skipping push to {}", title);
            return PushResult.SKIPPED;
        }
        if (!StringUtils.hasText(deviceToken)) {
            return PushResult.SKIPPED;
        }
        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                    .setWebpushConfig(WebpushConfig.builder()
                            .setNotification(WebpushNotification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .setIcon("/favicon.svg")
                                    .build())
                            .build())
                    .putAllData(data == null ? Map.of() : data)
                    .build();
            String id = FirebaseMessaging.getInstance().send(message);
            log.debug("FCM push sent ({}): {}", title, id);
            return PushResult.SENT;
        } catch (FirebaseMessagingException ex) {
            MessagingErrorCode code = ex.getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                // The device token is stale/invalid — drop it so we stop trying.
                log.info("FCM token invalid ({}), clearing it", code);
                tokenMaintenance.invalidate(deviceToken);
                return PushResult.INVALID_TOKEN;
            }
            log.warn("FCM push failed ({}): {}", code, ex.getMessage());
//            log.warn("FCM push failed ({}): {}", code, ex.getMessage(), ex);
            return PushResult.ERROR;
        } catch (Exception ex) {
            log.warn("FCM push failed: {}", ex.getMessage());
            return PushResult.ERROR;
        }
    }


    private Optional<Path> resolveCredentialsFile() {

        // 1: Explicit configuration wins.
        String configured = appProperties.getFcm().getServiceAccountFile();
        if (StringUtils.hasText(configured)) {
            Path path = Paths.get(configured);
            if (Files.exists(path))
                return Optional.of(path);

            log.warn("FCM_SERVICE_ACCOUNT_FILE is set to '{}' but the file does not exist", configured);
        }

        Path workingDir = Paths.get("").toAbsolutePath();

        // 2: Conventional filenames in the working directory
        for (String name : CONVENTIONAL_NAMES) {
            Path candidate = workingDir.resolve(name);
            if (Files.exists(candidate))
                return Optional.of(candidate);
        }

        // 3: Auto-discover any *firebase-adminsdk*.json in the working directory
        try (Stream<Path> stream = Files.list(workingDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String fn = p.getFileName().toString().toLowerCase();
                        return fn.endsWith(".json") && fn.contains("firebase") && fn.contains("adminsdk");
                    })
                    .min(Comparator.comparing(p -> p.getFileName().toString()));
        } catch (Exception ex) {
            log.debug("Could not scan working directory for a service-account file: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}
