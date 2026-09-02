package com.massseat.app.service;

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
    private final String botToken;
    private final String chatId;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TelegramLogService(
            @Value("${telegram.bot-token}") String botToken,
            @Value("${telegram.chat-id}") String chatId
    ) {
        this.botToken = botToken;
        this.chatId = chatId;

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

        if (botToken == null || botToken.isBlank()) {
            log.warn("Telegram bot token is missing.");
            return;
        }

        if (chatId == null || chatId.isBlank()) {
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
                    .uri("/bot{token}/sendMessage", botToken)
                    .body(Map.of(
                            "chat_id", chatId,
                            "text", text
                    ))
                    .retrieve()
                    .toBodilessEntity();

        } catch (Exception e) {

            /*
             * Telegram fail করলে আবার Telegram-এ error পাঠানোর চেষ্টা করা যাবে না।
             * এতে infinite loop হতে পারে।
             */
            log.error(
                    "Failed to send log to Telegram: {}",
                    e.getMessage()
            );
        }
    }
}

