package com.portfolio.manager.service;

import com.portfolio.manager.domain.Member;
import com.portfolio.manager.domain.Project;
import com.portfolio.manager.domain.ProjectStatus;
import com.portfolio.manager.dto.project.ProjectCreateRequest;
import com.portfolio.manager.dto.project.ProjectMembersUpdateRequest;
import com.portfolio.manager.dto.project.ProjectResponse;
import com.portfolio.manager.dto.project.ProjectFilterRequest;
import com.portfolio.manager.dto.project.ProjectStatusUpdateRequest;
import com.portfolio.manager.exception.BusinessRuleException;
import com.portfolio.manager.exception.NotFoundException;
import com.portfolio.manager.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    void shouldSetRealEndDateWhenClosingProjectWithoutRealEndDate() {
        Project project = project(1L, ProjectStatus.EM_ANDAMENTO);
        project.setRealEndDate(null);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectStatusUpdateRequest request = new ProjectStatusUpdateRequest();
        request.setStatus(ProjectStatus.ENCERRADO);

        Project updated = projectService.changeStatus(1L, request);

        assertEquals(ProjectStatus.ENCERRADO, updated.getStatus());
        assertNotNull(updated.getRealEndDate());
    }

    @Test
    void shouldDeleteProjectWhenStatusAllowsDeletion() {
        Project project = project(1L, ProjectStatus.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        verify(projectRepository).delete(project);
    }

    @Test
    void shouldRejectWhenPlannedEndDateIsBeforeStartDate() {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Projeto inválido");
        request.setStartDate(LocalDate.of(2026, 5, 10));
        request.setPlannedEndDate(LocalDate.of(2026, 5, 1));
        request.setBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Collections.singletonList(1L));

        assertThrows(BusinessRuleException.class, () -> projectService.create(request));
    }

    @Test
    void shouldRejectWhenRealEndDateIsBeforeStartDate() {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Projeto inválido");
        request.setStartDate(LocalDate.of(2026, 5, 10));
        request.setPlannedEndDate(LocalDate.of(2026, 6, 10));
        request.setRealEndDate(LocalDate.of(2026, 5, 1));
        request.setBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Collections.singletonList(1L));


        assertThrows(BusinessRuleException.class, () -> projectService.create(request));
    }

    @Test
    void shouldRejectWhenMemberListIsNull() {
        Member manager = member(1L, "Ana", "funcionário");
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Projeto sem membros");
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setPlannedEndDate(LocalDate.of(2026, 3, 1));
        request.setBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(null);

        when(memberService.findById(1L)).thenReturn(manager);
        when(memberService.isEmployee(manager)).thenReturn(true);
        when(projectRepository.countActiveProjectsByMemberId(eq(1L), any())).thenReturn(0L);

        assertThrows(BusinessRuleException.class, () -> projectService.create(request));
    }

    @Test
    void shouldRejectWhenMemberListExceedsMaximumLimit() {
        Member manager = member(1L, "Ana", "funcionário");
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Projeto com membros demais");
        request.setStartDate(LocalDate.of(2026, 1, 1));
        request.setPlannedEndDate(LocalDate.of(2026, 3, 1));
        request.setBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L));

        when(memberService.findById(1L)).thenReturn(manager);
        when(memberService.isEmployee(manager)).thenReturn(true);
        when(projectRepository.countActiveProjectsByMemberId(eq(1L), any())).thenReturn(0L);

        assertThrows(BusinessRuleException.class, () -> projectService.create(request));
    }

    @Test
    void shouldThrowNotFoundWhenProjectDoesNotExist() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> projectService.findById(999L));
    }

    @Test
    void shouldReturnMappedResponseWhenFindingProjectById() {
        Member manager = member(1L, "Ana", "funcionário");
        Member employee = member(2L, "João", "funcionário");
        Project project = project(10L, ProjectStatus.EM_ANALISE);
        project.setName("Projeto X");
        project.setStartDate(LocalDate.of(2026, 1, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 3, 1));
        project.setBudget(new BigDecimal("70000"));
        project.setManager(manager);
        project.setMembers(new HashSet<>(Collections.singletonList(employee)));

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.findResponseById(10L);

        assertEquals("Projeto X", response.getName());
        assertEquals("Ana", response.getManager().getName());
        assertEquals(1, response.getMembers().size());
    }

    @Test
    void shouldUpdateMembersThroughResponseMethod() {
        Member manager = member(1L, "Ana", "funcionário");
        Member memberA = member(2L, "João", "funcionário");
        Project project = project(20L, ProjectStatus.EM_ANALISE);
        project.setName("Projeto Y");
        project.setStartDate(LocalDate.of(2026, 1, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 4, 1));
        project.setBudget(new BigDecimal("90000"));
        project.setManager(manager);
        project.setMembers(new HashSet<>());

        ProjectMembersUpdateRequest request = new ProjectMembersUpdateRequest();
        request.setMemberIds(Collections.singletonList(2L));

        when(projectRepository.findById(20L)).thenReturn(Optional.of(project));
        when(memberService.findById(2L)).thenReturn(memberA);
        when(memberService.isEmployee(memberA)).thenReturn(true);
        when(projectRepository.countActiveProjectsByMemberIdExcludingProject(eq(2L), eq(20L), any())).thenReturn(0L);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = projectService.updateMembersResponse(20L, request);

        assertEquals(1, response.getMembers().size());
    }

    @Test
    void shouldSearchResponseKeepingPaginationMetadata() {
        Member manager = member(1L, "Ana", "funcionário");
        Project project = project(30L, ProjectStatus.EM_ANALISE);
        project.setName("Projeto Z");
        project.setStartDate(LocalDate.of(2026, 1, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 2, 1));
        project.setBudget(new BigDecimal("50000"));
        project.setManager(manager);
        project.setMembers(new HashSet<>());

        Page<Project> page = new PageImpl<>(Collections.singletonList(project), PageRequest.of(0, 10), 1);
        when(projectRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Project>>any(), any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        ProjectFilterRequest filter = new ProjectFilterRequest();
        Page<ProjectResponse> response = projectService.searchResponse(filter, PageRequest.of(0, 10));

        assertEquals(1, response.getTotalElements());
        assertFalse(response.getContent().isEmpty());
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

