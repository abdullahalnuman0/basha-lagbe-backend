package com.massseat.app.service.impl;

import com.massseat.app.entity.AppSetting;
import com.massseat.app.repository.AppSettingRepository;
import com.massseat.app.service.SettingsService;
import com.massseat.app.utls.SettingKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final AppSettingRepository repository;

    @Override
    @Transactional(readOnly = true)
    public String get(String key) {
        return repository.findByKey(key)
                .map(AppSetting::getValue)
                .filter(StringUtils::hasText)
                .orElseGet(() -> SettingKeys.DEFAULT.getOrDefault(key, ""));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    @Override
    @Transactional(readOnly = true)
    public int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
