package com.massseat.app.config;

import com.massseat.app.service.SettingsService;
import com.massseat.app.utls.SettingKeys;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JavaMailSender envMailSender;
    private final boolean envSmtpConfigured;
    private final String envFrom;

    private final SettingsService settings;


    @Override
    public void run(String @NonNull ... args) throws Exception {

        String to = "abdullahalnumanb@gmail.com",
                code = "123456",
                subject = "Mass Seat — আপনার ইমেইল ভেরিফিকেশন কোড",
                body = """
                আসসালামু আলাইকুম,
                
                আপনার Mass Seat OTP কোড: %s
                
                কোডটি ৫ মিনিটের জন্য কার্যকর। আপনি এই অনুরোধ না করে থাকলে ইমেইলটি উপেক্ষা করুন।
                
                — Mass Seat Team
                """.formatted(code);

        JavaMailSender sender = settings.getBoolean(SettingKeys.MAIL_ENABLED)
                ? resolveSender() : null;
        if (sender == null) {
            log.info("EMAIL->{}] {} - {}", to, subject, "OTP: " + code);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(resolveFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            sender.send(message);
            log.info("Email sent to {} ({}) code: {}", to, subject, code);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}{}", to, e.getMessage(),
                    " - fallback OTP: " + code);
        }
    }

    private @NotNull String resolveFrom() {
        String configured = settings.get(SettingKeys.MAIL_FROM);

        return StringUtils.hasText(configured) ? configured : envFrom;
    }

    private JavaMailSender resolveSender() {

        String host = settings.get(SettingKeys.MAIL_HOST);
        if (!StringUtils.hasText(host))
            return envSmtpConfigured ? envMailSender : null;

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setUsername(settings.get(SettingKeys.MAIL_USERNAME));
        sender.setPassword(settings.get(SettingKeys.MAIL_PASSWORD));
        sender.setPort(settings.getInt(
                        SettingKeys.MAIL_PORT,
                        Integer.parseInt(SettingKeys.DEFAULT.getOrDefault(SettingKeys.MAIL_PORT, "587"))
                )
        );
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(StringUtils.hasText(settings.get(SettingKeys.MAIL_USERNAME))));
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return sender;
    }
}
