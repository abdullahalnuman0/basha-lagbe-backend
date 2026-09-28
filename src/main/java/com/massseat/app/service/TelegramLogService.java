package com.massseat.app.service;

import com.massseat.app.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
public class TelegramLogService {

    private final RestClient restClient;

    private AppProperties.Telegram telegram;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TelegramLogService(
            AppProperties appProperties
    ) {
        this.telegram = appProperties.getTelegram();

        this.restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org")
                .build();
    }

    /**
     * Send INFO log to Telegram
     */
    @Async
    public void info(String message) {
        send("ℹ️ INFO", message);
    }

    /**
     * Send WARNING log to Telegram
     */
    @Async
    public void warn(String message) {
        send("⚠️ WARNING", message);
    }

    /**
     * Send ERROR log to Telegram
     */
    @Async
    public void error(String message) {
        send("🚨 ERROR", message);
    }

    /**
     * Send ERROR log with Exception
     */
    @Async
    public void error(String message, Throwable throwable) {

        String errorMessage = message
                + "\n\nException: "
                + throwable.getClass().getSimpleName()
                + "\nMessage: "
                + throwable.getMessage();

        send("🚨 ERROR", errorMessage);
    }

    private void send(String level, String message) {

        if (telegram.isLogEnable()) {
            log.info("Telegram log service is not enable");
            return;
        }

        if (telegram.getBotToken() == null || telegram.getBotToken().isBlank()) {
            log.warn("Telegram bot token is missing.");
            return;
        }

        if (telegram.getChatId() == null || telegram.getChatId().isBlank()) {
            log.warn("Telegram chat ID is missing.");
            return;
        }

        String time = LocalDateTime.now().format(DATE_FORMAT);

        String text = """
                %s
                
                🕐 Time: %s
                📦 Application: DormEasy
                🌍 Environment: Development
                
                %s
                """.formatted(
                level,
                time,
                message
        );

        try {

            restClient.post()
                    .uri("/bot{token}/sendMessage", telegram.getBotToken())
                    .body(Map.of(
                            "chat_id", telegram.getChatId(),
                            "text", text
                    ))
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {

            log.error(
                    "Failed to send log to Telegram: {}",
                    e.getMessage()
            );
        }
    }
}

