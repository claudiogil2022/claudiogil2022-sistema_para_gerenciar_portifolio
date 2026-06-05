package com.portfolio.manager.dto.project;

import com.portfolio.manager.domain.ProjectStatus;
import com.portfolio.manager.domain.RiskLevel;
import com.portfolio.manager.dto.member.MemberResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProjectResponse {

    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate plannedEndDate;
    private LocalDate realEndDate;
    private BigDecimal budget;
    private String description;
    private MemberResponse manager;
    private ProjectStatus status;
    private RiskLevel riskLevel;
    private List<MemberResponse> members = new ArrayList<MemberResponse>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(LocalDate plannedEndDate) {
        this.plannedEndDate = plannedEndDate;
    }

    public LocalDate getRealEndDate() {
        return realEndDate;
    }

    public void setRealEndDate(LocalDate realEndDate) {
        this.realEndDate = realEndDate;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MemberResponse getManager() {
        return manager;
    }

    public void setManager(MemberResponse manager) {
        this.manager = manager;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<MemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<MemberResponse> members) {
        this.members = members;
    }
}

