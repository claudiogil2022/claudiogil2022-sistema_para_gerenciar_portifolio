package com.portfolio.manager.service;

import com.portfolio.manager.domain.RiskLevel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectRiskServiceTest {

    @Test
    void shouldReturnLowRiskWhenBudgetAndDurationAreSmall() {
        RiskLevel riskLevel = ProjectRiskService.calculateRisk(new BigDecimal("100000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1));
        assertEquals(RiskLevel.LOW, riskLevel);
    }

    @Test
    void shouldReturnMediumRiskWhenBudgetOrDurationAreMedium() {
        RiskLevel riskLevel = ProjectRiskService.calculateRisk(new BigDecimal("250000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1));
        assertEquals(RiskLevel.MEDIUM, riskLevel);
    }

    @Test
    void shouldReturnHighRiskWhenBudgetOrDurationExceedsThreshold() {
        RiskLevel riskLevel = ProjectRiskService.calculateRisk(new BigDecimal("700000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 1));
        assertEquals(RiskLevel.HIGH, riskLevel);
    }
}

