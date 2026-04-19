package com.example.meridian.service;

import com.example.meridian.domain.entity.MeridianRuleConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 规则查询服务 */
@Service
@RequiredArgsConstructor
public class MeridianRuleConfigService {
    private final MeridianRuleLoaderService loader;
    public List<MeridianRuleConfig> listEnabled(){ return loader.listEnabledRules(); }
}
