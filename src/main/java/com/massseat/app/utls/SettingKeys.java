package com.massseat.app.utls;

import java.util.Map;

public final class SettingKeys {

    // --- Email (SMTP) overrides; blank = fall back to env config ---
    public static final String MAIL_HOST = "mail.host";
    public static final String MAIL_PORT = "mail.port";
    public static final String MAIL_USERNAME = "mail.username";
    public static final String MAIL_PASSWORD = "mail.password";
    public static final String MAIL_FROM = "mail.from";
    public static final String MAIL_ENABLED = "mail.enabled";


    // --- Setting default value ---
    public static final Map<String, String> DEFAULT = Map.ofEntries(
            Map.entry(MAIL_HOST,""),
            Map.entry(MAIL_PORT,"587"),
            Map.entry(MAIL_USERNAME,""),
            Map.entry(MAIL_PASSWORD,""),
            Map.entry(MAIL_FROM,""),
            Map.entry(MAIL_ENABLED, "true")
    );

}
