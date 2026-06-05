package com.portfolio.manager.dto.project;

import com.portfolio.manager.domain.ProjectStatus;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public class PortfolioReportResponse {

    private Map<ProjectStatus, Long> projectsByStatus = new EnumMap<ProjectStatus, Long>(ProjectStatus.class);
    private Map<ProjectStatus, BigDecimal> budgetByStatus = new EnumMap<ProjectStatus, BigDecimal>(ProjectStatus.class);
    private BigDecimal averageDurationOfClosedProjectsDays = BigDecimal.ZERO;
    private Long totalUniqueMembersAllocated = 0L;

    public Map<ProjectStatus, Long> getProjectsByStatus() {
        return projectsByStatus;
    }

    public void setProjectsByStatus(Map<ProjectStatus, Long> projectsByStatus) {
        this.projectsByStatus = projectsByStatus;
    }

    public Map<ProjectStatus, BigDecimal> getBudgetByStatus() {
        return budgetByStatus;
    }

    public void setBudgetByStatus(Map<ProjectStatus, BigDecimal> budgetByStatus) {
        this.budgetByStatus = budgetByStatus;
    }

    public BigDecimal getAverageDurationOfClosedProjectsDays() {
        return averageDurationOfClosedProjectsDays;
    }

    public void setAverageDurationOfClosedProjectsDays(BigDecimal averageDurationOfClosedProjectsDays) {
        this.averageDurationOfClosedProjectsDays = averageDurationOfClosedProjectsDays;
    }

    public Long getTotalUniqueMembersAllocated() {
        return totalUniqueMembersAllocated;
    }

    public void setTotalUniqueMembersAllocated(Long totalUniqueMembersAllocated) {
        this.totalUniqueMembersAllocated = totalUniqueMembersAllocated;
    }
}

