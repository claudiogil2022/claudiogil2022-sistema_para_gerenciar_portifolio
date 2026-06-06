package com.portfolio.manager.service;

import com.portfolio.manager.domain.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Serviço utilitário para classificação de risco de projetos.
 *
 * Implementa lógica de negócio que classifica projetos em 3 níveis (BAIXO, MEDIO, ALTO)
 * considerando dois fatores principais:
 * - Orçamento: projetos com maior investimento têm maior complexidade
 * - Duração: projetos mais longos têm maior exposição a riscos
 *
 * Classificação:
 * - ALTO: orçamento > R$ 500k OU duração > 6 meses
 * - BAIXO: orçamento <= R$ 100k E duração <= 3 meses
 * - MEDIO: demais casos
 *
 * Nota: Limiares foram definidos empiricamente, podendo ser ajustados
 * conforme histórico de projetos passar a ser coletado.
 */
public final class ProjectRiskService {

    // Limiares de risco definidos em reunião com stakeholders
    private static final BigDecimal BUDGET_LOW_LIMIT = new BigDecimal("100000");
    private static final BigDecimal BUDGET_MEDIUM_LIMIT = new BigDecimal("500000");

    private ProjectRiskService() {
    }

    /**
     * Calcula o nível de risco de um projeto.
     *
     * Algoritmo:
     * 1. Se orçamento > 500k OU duração > 6 meses -> ALTO
     * 2. Se orçamento <= 100k E duração <= 3 meses -> BAIXO
     * 3. Caso contrário -> MEDIO
     *
     * @param budget orçamento aproximado do projeto
     * @param startDate data de início
     * @param plannedEndDate data de término planejado
     * @return nível de risco classificado
     */
    public static RiskLevel calculateRisk(BigDecimal budget, LocalDate startDate, LocalDate plannedEndDate) {
        long months = calculateDurationInMonths(startDate, plannedEndDate);
        boolean highRisk = budget.compareTo(BUDGET_MEDIUM_LIMIT) > 0 || months > 6;
        if (highRisk) {
            return RiskLevel.ALTO;
        }
        boolean lowRisk = budget.compareTo(BUDGET_LOW_LIMIT) <= 0 && months <= 3;
        if (lowRisk) {
            return RiskLevel.BAIXO;
        }
        return RiskLevel.MEDIO;
    }

    public static long calculateDurationInMonths(LocalDate startDate, LocalDate plannedEndDate) {
        if (startDate == null || plannedEndDate == null) {
            return 0L;
        }
        long days = ChronoUnit.DAYS.between(startDate, plannedEndDate);
        if (days <= 0) {
            return 0L;
        }
        return (days + 29L) / 30L;
    }
}

