package com.massseat.app.service;

import com.massseat.app.config.AppProperties;
import com.massseat.app.entity.enums.OtpPurpose;
import com.massseat.app.utls.SettingKeys;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class ResendEmailService {

    private final String envFrom;
    private final SettingsService settings;

    private final String resendPassword;

    public ResendEmailService(
            // ObjectProvider<JavaMailSender> mailSenderProvider, // কমেন্টেড
            @Value("${spring.mail.password:}") String resendPassword,
            AppProperties properties,
            SettingsService settings
    ) {
        this.resendPassword = resendPassword;
        this.envFrom = properties.getMail().getFrom();
        this.settings = settings;

        log.info("EmailService: using Resend API for email delivery. "
                + "Set MAIL_ENABLED=true and MAIL_PASSWORD=your_resend_api_key in Admin Settings.");
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
            sendWithResend(to, subject, body);
            log.info("Email sent to {} ({}) code: {}", to, subject, otpForFallbackLog);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {} - fallback OTP: {}",
                    to, e.getMessage(), otpForFallbackLog);
        }
    }


    private void sendWithResend(String to, String subject, String textBody) {
        String apiKey = resendPassword == null || resendPassword.isEmpty()
                ? settings.get(SettingKeys.MAIL_PASSWORD)
                : resendPassword;

        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("(" + apiKey + ")Resend API key is missing. Set MAIL_PASSWORD in settings.");
        }

        String from = resolveFrom();
        if (!StringUtils.hasText(from)) {
            throw new IllegalStateException("Mail FROM address is missing. Set MAIL_FROM in settings.");
        }

        // RestClient তৈরি – প্রতি কলেই নতুন করে তৈরি করছি (বা বিন হিসেবেও দেওয়া যায়)
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();

        // রিকোয়েস্ট বডি
        Map<String, Object> payload = Map.of(
                "from", from,
                "to", new String[]{to},
                "subject", subject,
                "text", textBody
        );

        // POST কল
        restClient.post()
                .uri("/emails")
                .body(payload)
                .retrieve()
                .toBodilessEntity(); // আমরা রেসপন্স বডি ব্যবহার করছি না, শুধু স্ট্যাটাস চেক করি
    }

    private @NotNull String resolveFrom() {
        String configured = settings.get(SettingKeys.MAIL_FROM);
        return StringUtils.hasText(configured) ? configured : envFrom;
    }


}