package com.ziwushichen.health.rule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 时辰规则注册中心。
 */
@Component
public class MeridianRuleConfigRegistry {

    private final ObjectMapper objectMapper;

    private final Map<MeridianTimeSlotEnum, MeridianRuleConfig> configMap = new ConcurrentHashMap<>();

    public MeridianRuleConfigRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        loadFromJson();
    }

    /**
     * 获取指定时辰规则。
     *
     * @param meridianTimeSlot 时辰
     * @return 规则配置
     */
    public MeridianRuleConfig getBySlot(MeridianTimeSlotEnum meridianTimeSlot) {
        return configMap.get(meridianTimeSlot);
    }

    /**
     * 获取全部规则。
     *
     * @return 规则列表
     */
    public List<MeridianRuleConfig> listAll() {
        return Collections.unmodifiableList(configMap.values().stream().collect(Collectors.toList()));
    }

    private void loadFromJson() {
        try {
            ClassPathResource classPathResource = new ClassPathResource("rules/meridian-rule-config.json");
            try (InputStream inputStream = classPathResource.getInputStream()) {
                List<MeridianRuleConfig> configList = objectMapper.readValue(inputStream, new TypeReference<List<MeridianRuleConfig>>() {
                });
                for (MeridianRuleConfig config : configList) {
                    MeridianTimeSlotEnum meridianTimeSlot = MeridianTimeSlotEnum.valueOf(config.getMeridianTimeSlotName());
                    configMap.put(meridianTimeSlot, config);
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("加载时辰规则配置失败", ex);
        }
    }
}
