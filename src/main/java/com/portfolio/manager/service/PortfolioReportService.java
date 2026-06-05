package com.portfolio.manager.service;

import com.portfolio.manager.domain.Project;
import com.portfolio.manager.domain.ProjectStatus;
import com.portfolio.manager.dto.project.PortfolioReportResponse;
import com.portfolio.manager.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class PortfolioReportService {

    private final ProjectRepository projectRepository;

    public PortfolioReportService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public PortfolioReportResponse generate() {
        List<Project> projects = projectRepository.findAll();
        PortfolioReportResponse response = new PortfolioReportResponse();
        Map<ProjectStatus, Long> projectsByStatus = new EnumMap<>(ProjectStatus.class);
        Map<ProjectStatus, BigDecimal> budgetByStatus = new EnumMap<>(ProjectStatus.class);
        Set<Long> uniqueMembers = new HashSet<>();

        BigDecimal totalClosedDurationDays = BigDecimal.ZERO;
        long closedProjects = 0L;

        for (ProjectStatus status : ProjectStatus.values()) {
            projectsByStatus.put(status, 0L);
            budgetByStatus.put(status, BigDecimal.ZERO);
        }

        for (Project project : projects) {
            projectsByStatus.put(project.getStatus(), Long.valueOf(projectsByStatus.get(project.getStatus()) + 1L));
            budgetByStatus.put(project.getStatus(), budgetByStatus.get(project.getStatus()).add(project.getBudget()));
            project.getMembers().forEach(member -> uniqueMembers.add(member.getId()));

            if (project.getStatus().equals(ProjectStatus.ENCERRADO) && project.getStartDate() != null && project.getRealEndDate() != null) {
                totalClosedDurationDays = totalClosedDurationDays.add(BigDecimal.valueOf(ChronoUnit.DAYS.between(project.getStartDate(), project.getRealEndDate())));
                closedProjects++;
            }
        }

        response.setProjectsByStatus(projectsByStatus);
        response.setBudgetByStatus(budgetByStatus);
        if (closedProjects > 0L) {
            response.setAverageDurationOfClosedProjectsDays(totalClosedDurationDays.divide(BigDecimal.valueOf(closedProjects), 2, RoundingMode.HALF_UP));
        }
        response.setTotalUniqueMembersAllocated((long) uniqueMembers.size());
        return response;
    }
}

