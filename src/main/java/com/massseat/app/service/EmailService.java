package com.massseat.app.service;

import com.massseat.app.config.AppProperties;
import com.massseat.app.entity.enums.OtpPurpose;
import com.massseat.app.utls.SettingKeys;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender envMailSender;
    private final boolean envSmtpConfigured;
    private final String envFrom;

    private final SettingsService settings;

    public EmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.host:}") String mailHost,
            AppProperties properties,
            SettingsService settings
    ) {
        this.envMailSender = mailSenderProvider.getIfAvailable();
        this.envSmtpConfigured = (this.envMailSender != null && StringUtils.hasText(mailHost));
        this.envFrom = properties.getMail().getFrom();
        this.settings = settings;

        if (envSmtpConfigured)
            log.info("EmailService: SMTP configured from env ({})", mailHost);
        else
            log.warn("EmailService: no SMTP host in env — set it in Admin → Settings, "
                    + "otherwise emails are logged to the console");

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
        JavaMailSender sender = settings.getBoolean(SettingKeys.MAIL_ENABLED)
                ? resolveSender() : null;
        if (sender == null) {
            log.info("EMAIL->{}] {} - {}", to, subject, otpForFallbackLog != null ? "OTP: " + otpForFallbackLog : body);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(resolveFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
//            sender.send(message); //todo: un comment this for email sending...
            log.info("Email sent to {} ({}) code: {}", to, subject,otpForFallbackLog);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}{}", to, e.getMessage(),
                    otpForFallbackLog != null ? " - fallback OTP: " + otpForFallbackLog : "");
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
