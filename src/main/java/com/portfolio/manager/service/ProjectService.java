package com.portfolio.manager.service;

import com.portfolio.manager.domain.Member;
import com.portfolio.manager.domain.Project;
import com.portfolio.manager.domain.ProjectStatus;
import com.portfolio.manager.domain.ProjectStatusFlow;
import com.portfolio.manager.domain.RiskLevel;
import com.portfolio.manager.dto.project.ProjectCreateRequest;
import com.portfolio.manager.dto.project.ProjectMembersUpdateRequest;
import com.portfolio.manager.dto.project.ProjectFilterRequest;
import com.portfolio.manager.dto.project.ProjectResponse;
import com.portfolio.manager.dto.project.ProjectStatusUpdateRequest;
import com.portfolio.manager.dto.project.ProjectUpdateRequest;
import com.portfolio.manager.exception.BusinessRuleException;
import com.portfolio.manager.exception.NotFoundException;
import com.portfolio.manager.mapper.ProjectMapper;
import com.portfolio.manager.repository.ProjectRepository;
import com.portfolio.manager.repository.ProjectSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Serviço central de gerenciamento de projetos.
 * 
 * Responsável por orquestrar a criação, atualização, exclusão e consulta de projetos,
 * aplicando todas as regras de negócio definidas pelo desafio técnico.
 * 
 * Decisões de design:
 * - Todas as operações são transacionais por padrão (@Transactional)
 * - Validações de regra de negócio acontecem em métodos privados antes da persistência
 * - Limites de membros e projetos ativos são constantes imutáveis para facilitar manutenção
 * 
 * TODO: Implementar cache de projetos ativos por membro para melhor performance em leitura
 * TODO: Adicionar auditoria de mudanças de status (quem, quando, por quê)
 */
@Service
@Transactional
public class ProjectService {

    // Limites configurados conforme requisito do desafio técnico
    private static final int MIN_MEMBERS = 1;
    private static final int MAX_MEMBERS = 10;
    private static final int MAX_ACTIVE_PROJECTS_PER_MEMBER = 3;
    
    // Status finais que bloqueiam operações destrutivas
    private static final Collection<ProjectStatus> TERMINAL_STATUSES = Collections.unmodifiableList(
            java.util.Arrays.asList(ProjectStatus.ENCERRADO, ProjectStatus.CANCELADO));

    private final ProjectRepository projectRepository;
    private final MemberService memberService;

    public ProjectService(ProjectRepository projectRepository, MemberService memberService) {
        this.projectRepository = projectRepository;
        this.memberService = memberService;
    }

    /**
     * Cria um novo projeto com validação completa de regras de negócio.
     * 
     * @param request dados de entrada com informações do projeto
     * @return projeto persistido com ID gerado
     * @throws BusinessRuleException se datas, limites ou atribuições violarem regras
     */
    public Project create(ProjectCreateRequest request) {
        validateDates(request.getStartDate(), request.getPlannedEndDate(), request.getRealEndDate());
        Project project = new Project();
        project.setName(request.getName().trim());
        project.setStartDate(request.getStartDate());
        project.setPlannedEndDate(request.getPlannedEndDate());
        project.setRealEndDate(request.getRealEndDate());
        project.setBudget(request.getBudget());
        project.setDescription(request.getDescription());
        project.setStatus(ProjectStatus.EM_ANALISE);
        project.setManager(resolveAndValidateManager(request.getManagerId(), null));
        project.setMembers(loadAndValidateMembers(request.getMemberIds(), null));
        return projectRepository.save(project);
    }

    public ProjectResponse createResponse(ProjectCreateRequest request) {
        return ProjectMapper.toResponse(create(request));
    }

    @Transactional(readOnly = true)
    public Project findById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Projeto não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public ProjectResponse findResponseById(Long id) {
        return ProjectMapper.toResponse(findById(id));
    }

    public Project update(Long id, ProjectUpdateRequest request) {
        validateDates(request.getStartDate(), request.getPlannedEndDate(), request.getRealEndDate());
        Project project = findById(id);
        project.setName(request.getName().trim());
        project.setStartDate(request.getStartDate());
        project.setPlannedEndDate(request.getPlannedEndDate());
        project.setRealEndDate(request.getRealEndDate());
        project.setBudget(request.getBudget());
        project.setDescription(request.getDescription());
        project.setManager(resolveAndValidateManager(request.getManagerId(), project.getId()));
        if (request.getMemberIds() != null) {
            project.setMembers(loadAndValidateMembers(request.getMemberIds(), project.getId()));
        }
        return projectRepository.save(project);
    }

    public ProjectResponse updateResponse(Long id, ProjectUpdateRequest request) {
        return ProjectMapper.toResponse(update(id, request));
    }

    public Project updateMembers(Long id, ProjectMembersUpdateRequest request) {
        Project project = findById(id);
        project.setMembers(loadAndValidateMembers(request.getMemberIds(), project.getId()));
        return projectRepository.save(project);
    }

    public ProjectResponse updateMembersResponse(Long id, ProjectMembersUpdateRequest request) {
        return ProjectMapper.toResponse(updateMembers(id, request));
    }

    public void delete(Long id) {
        Project project = findById(id);
        // Bloqueio de exclusão em estados críticos (EM_ANDAMENTO, ENCERRADO)
        // Prevenção de perda acidental de dados de projeto que já iniciou
        if (ProjectStatusFlow.isDeletionRestricted(project.getStatus())) {
            throw new BusinessRuleException("Projeto não pode ser excluído quando estiver em iniciado, em andamento ou encerrado");
        }
        projectRepository.delete(project);
    }

    /**
     * Altera o status de um projeto conforme fluxo definido.
     * Garante transições válidas e atualiza data real de término ao encerrar.
     * 
     * @param id identificador do projeto
     * @param request novo status desejado
     * @return projeto com status atualizado
     * @throws BusinessRuleException se transição for inválida
     */
    public Project changeStatus(Long id, ProjectStatusUpdateRequest request) {
        Project project = findById(id);
        ProjectStatus current = project.getStatus();
        ProjectStatus target = request.getStatus();
        if (!ProjectStatusFlow.isValidTransition(current, target)) {
            throw new BusinessRuleException("Transição de status inválida: " + current + " -> " + target);
        }
        project.setStatus(target);
        if (target == ProjectStatus.ENCERRADO && project.getRealEndDate() == null) {
            project.setRealEndDate(LocalDate.now());
        }
        return projectRepository.save(project);
    }

    public ProjectResponse changeStatusResponse(Long id, ProjectStatusUpdateRequest request) {
        return ProjectMapper.toResponse(changeStatus(id, request));
    }

    @Transactional(readOnly = true)
    public Page<Project> search(ProjectFilterRequest filter, Pageable pageable) {
        Specification<Project> specification = Specification
                .where(ProjectSpecifications.withName(filter.getName()))
                .and(ProjectSpecifications.withStatus(filter.getStatus()))
                .and(ProjectSpecifications.withManagerName(filter.getManagerName()))
                .and(ProjectSpecifications.withMinBudget(filter.getMinBudget()))
                .and(ProjectSpecifications.withMaxBudget(filter.getMaxBudget()))
                .and(ProjectSpecifications.withStartDateFrom(filter.getStartDateFrom()))
                .and(ProjectSpecifications.withStartDateTo(filter.getStartDateTo()))
                .and(ProjectSpecifications.withEndDateFrom(filter.getEndDateFrom()))
                .and(ProjectSpecifications.withEndDateTo(filter.getEndDateTo()));

        Page<Project> page = projectRepository.findAll(specification, pageable);
        if (filter.getRiskLevel() == null) {
            return page;
        }

        List<Project> filtered = new ArrayList<>();
        for (Project project : page.getContent()) {
            RiskLevel riskLevel = ProjectRiskService.calculateRisk(project.getBudget(), project.getStartDate(), project.getPlannedEndDate());
            if (filter.getRiskLevel().equals(riskLevel)) {
                filtered.add(project);
            }
        }
        return new PageImpl<>(filtered, pageable, filtered.size());
    }

    @Transactional(readOnly = true)
    public Page<ProjectResponse> searchResponse(ProjectFilterRequest filter, Pageable pageable) {
        return search(filter, pageable).map(ProjectMapper::toResponse);
    }

    private Set<Member> loadAndValidateMembers(List<Long> memberIds, Long projectId) {
        if (memberIds == null) {
            throw new BusinessRuleException("A lista de membros é obrigatória");
        }
        
        // Elimina duplicatas usando Set para evitar alocações múltiplas do mesmo membro
        Set<Long> uniqueIds = new HashSet<>(memberIds);
        if (uniqueIds.isEmpty()) {
            throw new BusinessRuleException("O projeto deve possuir no mínimo 1 membro");
        }
        if (uniqueIds.size() > MAX_MEMBERS) {
            throw new BusinessRuleException("O projeto deve possuir no máximo 10 membros");
        }

        Set<Member> members = new HashSet<>();
        for (Long memberId : uniqueIds) {
            Member member = memberService.findById(memberId);
            
            // Validação crítica: apenas funcionários podem ser alocados
            // Contractors/consultores são excluídos por política da organização
            if (!memberService.isEmployee(member)) {
                throw new BusinessRuleException("Somente membros com atribuição funcionário podem ser associados");
            }
            
            // Previne sobrecarga: cada funcionário pode estar em no máximo 3 projetos ativos
            validateMemberProjectLimit(memberId, projectId);
            members.add(member);
        }
        return members;
    }

    private Member resolveAndValidateManager(Long managerId, Long projectId) {
        Member manager = memberService.findById(managerId);
        if (!memberService.isEmployee(manager)) {
            throw new BusinessRuleException("O gerente responsável deve possuir atribuição funcionário");
        }
        validateMemberProjectLimit(managerId, projectId);
        return manager;
    }

    /**
     * Valida limite de projetos ativos por membro com suporte a operações de atualização.
     * Ao criar: projectId é null (novo projeto)
     * Ao atualizar: projectId é o projeto atual (excluído da contagem)
     */
    private void validateMemberProjectLimit(Long memberId, Long projectId) {
        long activeProjects = projectId == null
                ? projectRepository.countActiveProjectsByMemberId(memberId, TERMINAL_STATUSES)
                : projectRepository.countActiveProjectsByMemberIdExcludingProject(memberId, projectId, TERMINAL_STATUSES);
        long projectedTotal = activeProjects + 1L;
        if (projectedTotal > MAX_ACTIVE_PROJECTS_PER_MEMBER) {
            throw new BusinessRuleException("O membro " + memberId + " já está alocado em 3 projetos ativos");
        }
    }

    private void validateDates(LocalDate startDate, LocalDate plannedEndDate, LocalDate realEndDate) {
        if (startDate == null || plannedEndDate == null) {
            throw new BusinessRuleException("Datas de início e previsão de término são obrigatórias");
        }
        if (plannedEndDate.isBefore(startDate)) {
            throw new BusinessRuleException("A previsão de término não pode ser anterior à data de início");
        }
        if (realEndDate != null && realEndDate.isBefore(startDate)) {
            throw new BusinessRuleException("A data real de término não pode ser anterior à data de início");
        }
    }
}

