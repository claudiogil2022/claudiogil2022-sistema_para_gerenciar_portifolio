package com.portfolio.manager.config;

import com.portfolio.manager.domain.RiskLevel;
import com.portfolio.manager.domain.TextNormalizer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class RiskLevelConverter implements Converter<String, RiskLevel> {

    @Override
    public RiskLevel convert(@NonNull String source) {

        String normalized = TextNormalizer.normalize(source);
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }

        switch (normalized) {
            case "baixo":
            case "low":
                return RiskLevel.BAIXO;
            case "medio":
            case "medium":
            case "mediu":
                return RiskLevel.MEDIO;
            case "alto":
            case "high":
            case "hight":
                return RiskLevel.ALTO;
            default:
                throw new IllegalArgumentException("RiskLevel inválido. Use BAIXO, MEDIO ou ALTO.");
        }
    }
}

