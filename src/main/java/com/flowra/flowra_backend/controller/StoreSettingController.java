package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.entity.StoreSetting;
import com.flowra.flowra_backend.repository.StoreSettingRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class StoreSettingController {

    private final StoreSettingRepository settingRepository;

    @Data
    public static class SettingEntry {
        private String key;
        private String value;
        private String group;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> getAllSettings() {
        Map<String, String> map = new HashMap<>();
        settingRepository.findAll().forEach(s -> map.put(s.getSettingKey(), s.getSettingValue()));
        return ResponseEntity.ok(map);
    }

    @PostMapping
    public ResponseEntity<?> saveSettings(@RequestBody Map<String, Object> settings) {
        settings.forEach((key, val) -> {
            if (key != null && val != null) {
                StoreSetting setting = settingRepository.findBySettingKey(key)
                        .orElseGet(() -> StoreSetting.builder().settingKey(key).settingGroup("general").build());
                setting.setSettingValue(val.toString());
                settingRepository.save(setting);
            }
        });
        return ResponseEntity.ok(Map.of("message", "Settings saved successfully!"));
    }
}
