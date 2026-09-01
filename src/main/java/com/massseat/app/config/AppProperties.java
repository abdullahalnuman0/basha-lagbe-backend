package com.massseat.app.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Otp otp = new Otp();
    private final Mail mail = new Mail();
    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();

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

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private Duration accessTokenTtl = Duration.ofMinutes(15);
        private Duration refreshTokenTtl = Duration.ofDays(30);
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>();
    }
}
