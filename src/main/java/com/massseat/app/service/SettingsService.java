package com.massseat.app.service;

public interface SettingsService {

    /**
     * Retrieves a setting value by key.
     * Returns the configured database value when available;
     * otherwise, falls back to the default value.
     */
    String get(String key);

    /**
     * Retrieves a setting value by key and converts it to a boolean.
     * Returns {@code false} when the value is missing or not a valid boolean.
     */
    boolean getBoolean(String key);

    /**
     * Retrieves a setting value by key and converts it to an integer.
     * Returns {@code 0} when the value is missing or cannot be parsed as an integer.
     */
    /**
     * Retrieves a setting value by key and parses it as an integer.
     * Returns the provided fallback value when the setting is missing or invalid.
     */
    int getInt(String key, int fallback);
}
