package com.massseat.app.service;

import com.massseat.app.config.AppProperties;
import com.massseat.app.entity.enums.OtpPurpose;
import com.massseat.app.utils.SettingKeys;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BrevoEmailService {

    private final String envFrom;
    private final SettingsService settings;
    private final String resendPassword;

    private final TelegramLogService telegramLog;


    public BrevoEmailService(
            @Value("${spring.mail.password:}") String resendPassword,
            AppProperties properties,
            SettingsService settings,
            TelegramLogService telegramLog
    ) {
        this.resendPassword = resendPassword;
        this.envFrom = properties.getMail().getFrom();
        this.settings = settings;
        this.telegramLog = telegramLog;
        log.info("EmailService: using Brevo API for email delivery. "
                + "Set MAIL_ENABLED=true, MAIL_FROM=your_verified_email, and MAIL_PASSWORD=your_brevo_api_key in Admin Settings.");
    }

    @Async
    public void sendOtp(String to, String code, String purpose) {
        String subject = OtpPurpose.REGISTRATION.name().equals(purpose)
                ? "Mass Seat — আপনার ইমেইল ভেরিফিকেশন কোড"
                : "Mass Seat — পাসওয়ার্ড রিসেট কোড";

        String body = """
                আসসালামু আলাইকুম,
                
                আপনার Mass Seat OTP কোড: %s
                
                কোডটি ৫ মিনিটের জন্য কার্যকর। আপনি এই অনুরোধ না করে থাকলে ইমেইলটি উপেক্ষা করুন।
                
                — Mass Seat Team
                """.formatted(code);

        deliver(to, subject, body, code);
    }


    private void deliver(String to, String subject, String body, String otpForFallbackLog) {
        boolean mailEnabled = settings.getBoolean(SettingKeys.MAIL_ENABLED);
        if (!mailEnabled) {
            log.info("EMAIL->{}] {} - {}", to, subject,
                    otpForFallbackLog != null ? "OTP: " + otpForFallbackLog : body);
            return;
        }

        try {
            sendWithBrevo(to, subject, body);
            log.info("Email sent to {} ({}) code: {}", to, subject, otpForFallbackLog);
            telegramLog.info("""
                    Email send successfully
                    
                    To: %s
                    
                    Subject: %s
                    
                    Body: %s",
                    """.formatted(to, subject, body));
        } catch (Exception e) {
            log.error("Failed to send email to {}: {} - fallback OTP: {}",
                    to, e.getMessage(), otpForFallbackLog);
            telegramLog.error("""
                    Brevo email sending failed!
                    
                    Error: %s
                    
                    To: %s
                    
                    Subject: %s
                    """.formatted(e.getMessage(), to, subject));
        }
    }


    private void sendWithBrevo(String to, String subject, String textBody) {
        String apiKey = resendPassword == null || resendPassword.isEmpty()
                ? settings.get(SettingKeys.MAIL_PASSWORD)
                : resendPassword;

        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Brevo API key is missing. Set MAIL_PASSWORD in settings.");
        }

        String fromRaw = resolveFrom();
        if (!StringUtils.hasText(fromRaw)) {
            throw new IllegalStateException("Mail FROM address is missing. Set MAIL_FROM in settings.");
        }

        // --- separate name & email ---
        String senderName = null;
        String senderEmail = fromRaw;

        // if formate "Name <email@domain.com>" the well be parsed
        if (fromRaw.contains("<") && fromRaw.contains(">")) {
            int startName = 0;
            int endName = fromRaw.indexOf('<');
            senderName = fromRaw.substring(startName, endName).trim();
            int startEmail = fromRaw.indexOf('<') + 1;
            int endEmail = fromRaw.indexOf('>');
            senderEmail = fromRaw.substring(startEmail, endEmail).trim();
        }
        // if only email, then senderName = null , (using Brevo default)

        // RestClient create
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3")
                .defaultHeader("api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        // payload create
        Map<String, Object> senderMap = Map.of("email", senderEmail);
        if (senderName != null && !senderName.isEmpty()) {
            senderMap = Map.of("email", senderEmail, "name", senderName);
        }

        Map<String, Object> payload = Map.of(
                "sender", senderMap,
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", textBody
        );

        restClient.post()
                .uri("/smtp/email")
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private @NotNull String resolveFrom() {
        String configured = settings.get(SettingKeys.MAIL_FROM);
        return StringUtils.hasText(configured) ? configured : envFrom;
    }
}
