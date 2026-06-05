package com.portfolio.manager.service;

import com.portfolio.manager.domain.Member;
import com.portfolio.manager.domain.Project;
import com.portfolio.manager.domain.ProjectStatus;
import com.portfolio.manager.dto.project.PortfolioReportResponse;
import com.portfolio.manager.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioReportServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Test
    void shouldGeneratePortfolioSummary() {
        PortfolioReportService service = new PortfolioReportService(projectRepository);
        Project closed = project(1L, ProjectStatus.ENCERRADO, new BigDecimal("1000"), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 1L, 2L);
        Project active = project(2L, ProjectStatus.EM_ANDAMENTO, new BigDecimal("2000"), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1), 2L, 3L);
        when(projectRepository.findAll()).thenReturn(Arrays.asList(closed, active));

        PortfolioReportResponse response = service.generate();

        assertEquals(1L, response.getProjectsByStatus().get(ProjectStatus.ENCERRADO));
        assertEquals(new BigDecimal("1000"), response.getBudgetByStatus().get(ProjectStatus.ENCERRADO));
        assertEquals(new BigDecimal("30.00"), response.getAverageDurationOfClosedProjectsDays());
        assertEquals(3L, response.getTotalUniqueMembersAllocated());
    }

    private Project project(Long id, ProjectStatus status, BigDecimal budget, LocalDate startDate, LocalDate realEndDate, Long... memberIds) {
        Project project = new Project();
        project.setId(id);
        project.setStatus(status);
        project.setBudget(budget);
        project.setStartDate(startDate);
        project.setRealEndDate(realEndDate);
        List<Member> members = Arrays.asList(newMember(memberIds[0]), newMember(memberIds[1]));
        project.setMembers(new HashSet<Member>(members));
        return project;
    }

    private Member newMember(Long id) {
        Member member = new Member();
        member.setId(id);
        return member;
    }
}

