package com.massseat.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Getter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Otp otp = new Otp();
    private final Mail mail = new Mail();

    @Getter
    @Setter
    public static class Otp {
        private int expiryMinutes = 5;

        private int maxPerHour = 6;

        private int resendCooldownSeconds = 45;

        private int maxAttempts = 5;
    }

    @Getter
    @Setter
    public static class Mail {
        private String from = "Mess Seat <no-replay@messseat.com>";
    }
}
