package com.portfolio.manager.service;

import com.portfolio.manager.domain.Member;
import com.portfolio.manager.domain.Project;
import com.portfolio.manager.domain.ProjectStatus;
import com.portfolio.manager.dto.project.ProjectCreateRequest;
import com.portfolio.manager.dto.project.ProjectStatusUpdateRequest;
import com.portfolio.manager.exception.BusinessRuleException;
import com.portfolio.manager.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberService memberService;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, memberService);
    }

    @Test
    void shouldCreateProjectWithEmployeeMembers() {
        Member manager = member(1L, "Ana", "funcionário");
        Member employee = member(2L, "João", "funcionário");
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Projeto A");
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setPlannedEndDate(LocalDate.of(2026, 3, 1));
        request.setBudget(new BigDecimal("50000"));
        request.setDescription("Descrição");
        request.setManagerId(1L);
        request.setMemberIds(Collections.singletonList(2L));

        when(memberService.findById(1L)).thenReturn(manager);
        when(memberService.findById(2L)).thenReturn(employee);
        when(memberService.isEmployee(manager)).thenReturn(true);
        when(memberService.isEmployee(employee)).thenReturn(true);
        when(projectRepository.countActiveProjectsByMemberId(eq(1L), any())).thenReturn(0L);
        when(projectRepository.countActiveProjectsByMemberId(eq(2L), any())).thenReturn(0L);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Project project = projectService.create(request);

        assertEquals(ProjectStatus.EM_ANALISE, project.getStatus());
        assertEquals(1, project.getMembers().size());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void shouldRejectMemberThatIsNotEmployee() {
        Member manager = member(1L, "Ana", "funcionário");
        Member contractor = member(2L, "João", "terceiro");
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Projeto A");
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setPlannedEndDate(LocalDate.of(2026, 3, 1));
        request.setBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Collections.singletonList(2L));

        when(memberService.findById(1L)).thenReturn(manager);
        when(memberService.findById(2L)).thenReturn(contractor);
        when(memberService.isEmployee(manager)).thenReturn(true);
        when(memberService.isEmployee(contractor)).thenReturn(false);
        when(projectRepository.countActiveProjectsByMemberId(eq(1L), any())).thenReturn(0L);

        assertThrows(BusinessRuleException.class, () -> projectService.create(request));
    }

    @Test
    void shouldRejectStatusJump() {
        Project project = project(1L, ProjectStatus.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest();
        request.setStatus(ProjectStatus.INICIADO);

        assertThrows(BusinessRuleException.class, () -> projectService.changeStatus(1L, request));
    }

    @Test
    void shouldPreventDeletionForActiveStatuses() {
        Project project = project(1L, ProjectStatus.INICIADO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(BusinessRuleException.class, () -> projectService.delete(1L));
    }

    @Test
    void shouldRejectWhenMemberAlreadyHasThreeActiveProjects() {
        Member manager = member(1L, "Ana", "funcionário");
        Member employee = member(2L, "João", "funcionário");
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Projeto A");
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setPlannedEndDate(LocalDate.of(2026, 3, 1));
        request.setBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Collections.singletonList(2L));

        when(memberService.findById(1L)).thenReturn(manager);
        when(memberService.findById(2L)).thenReturn(employee);
        when(memberService.isEmployee(manager)).thenReturn(true);
        when(memberService.isEmployee(employee)).thenReturn(true);
        when(projectRepository.countActiveProjectsByMemberId(eq(1L), any())).thenReturn(0L);
        when(projectRepository.countActiveProjectsByMemberId(eq(2L), any())).thenReturn(3L);

        assertThrows(BusinessRuleException.class, () -> projectService.create(request));
    }

    @Test
    void shouldCountManagerAndMemberParticipationTogetherWhenRejectingFourthActiveProject() {
        Member manager = member(1L, "Ana", "funcionário");
        Member employee = member(2L, "João", "funcionário");
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Projeto A");
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setPlannedEndDate(LocalDate.of(2026, 3, 1));
        request.setBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Collections.singletonList(2L));

        when(memberService.findById(1L)).thenReturn(manager);
        when(memberService.findById(2L)).thenReturn(employee);
        when(memberService.isEmployee(manager)).thenReturn(true);
        when(memberService.isEmployee(employee)).thenReturn(true);
        when(projectRepository.countActiveProjectsByMemberId(eq(1L), any())).thenReturn(0L);
        when(projectRepository.countActiveProjectsByMemberId(eq(2L), any())).thenReturn(3L);

        assertThrows(BusinessRuleException.class, () -> projectService.create(request));
    }

    @Test
    void shouldRejectManagerThatIsNotEmployee() {
        Member manager = member(1L, "Ana", "gerente");
        Member employee = member(2L, "João", "funcionário");
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Projeto A");
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setPlannedEndDate(LocalDate.of(2026, 3, 1));
        request.setBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Collections.singletonList(2L));

        when(memberService.findById(1L)).thenReturn(manager);
        when(memberService.isEmployee(manager)).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> projectService.create(request));
    }

    @Test
    void shouldRejectWhenManagerAlreadyHasThreeActiveProjects() {
        Member manager = member(1L, "Ana", "funcionário");
        Member employee = member(2L, "João", "funcionário");
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Projeto A");
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setPlannedEndDate(LocalDate.of(2026, 3, 1));
        request.setBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Collections.singletonList(2L));

        when(memberService.findById(1L)).thenReturn(manager);
        when(memberService.isEmployee(manager)).thenReturn(true);
        when(projectRepository.countActiveProjectsByMemberId(eq(1L), any())).thenReturn(3L);

        assertThrows(BusinessRuleException.class, () -> projectService.create(request));
    }

    private Member member(Long id, String name, String role) {
        Member member = new Member();
        member.setId(id);
        member.setName(name);
        member.setRole(role);
        return member;
    }

    private Project project(Long id, ProjectStatus status) {
        Project project = new Project();
        project.setId(id);
        project.setStatus(status);
        return project;
    }
}

