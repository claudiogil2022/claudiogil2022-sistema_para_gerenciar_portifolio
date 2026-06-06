package com.portfolio.manager.controller;

import com.portfolio.manager.dto.project.PortfolioReportResponse;
import com.portfolio.manager.dto.project.ProjectCreateRequest;
import com.portfolio.manager.dto.project.ProjectMembersUpdateRequest;
import com.portfolio.manager.dto.project.ProjectFilterRequest;
import com.portfolio.manager.dto.project.ProjectResponse;
import com.portfolio.manager.dto.project.ProjectStatusUpdateRequest;
import com.portfolio.manager.dto.project.ProjectUpdateRequest;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import com.portfolio.manager.service.PortfolioReportService;
import com.portfolio.manager.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final PortfolioReportService reportService;

    public ProjectController(ProjectService projectService, PortfolioReportService reportService) {
        this.projectService = projectService;
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "Cria um novo projeto")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createResponse(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um projeto pelo identificador")
    public ProjectResponse findById(@PathVariable Long id) {
        return projectService.findResponseById(id);
    }

    @GetMapping
    @Operation(summary = "Lista projetos com paginação e filtros")
    public Page<ProjectResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) com.portfolio.manager.domain.ProjectStatus status,
            @Parameter(
                    description = "Filtro de risco do projeto. Valores: BAIXO, MEDIO, ALTO.",
                    schema = @Schema(allowableValues = {"BAIXO", "MEDIO", "ALTO"})
            )
            @RequestParam(required = false) com.portfolio.manager.domain.RiskLevel riskLevel,
            @RequestParam(required = false) String managerName,
            @RequestParam(required = false) java.math.BigDecimal minBudget,
            @RequestParam(required = false) java.math.BigDecimal maxBudget,
            @RequestParam(required = false) java.time.LocalDate startDateFrom,
            @RequestParam(required = false) java.time.LocalDate startDateTo,
            @RequestParam(required = false) java.time.LocalDate endDateFrom,
            @RequestParam(required = false) java.time.LocalDate endDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, direction, sortParts[0]);
        ProjectFilterRequest filter = new ProjectFilterRequest();
        filter.setName(name);
        filter.setStatus(status);
        filter.setRiskLevel(riskLevel);
        filter.setManagerName(managerName);
        filter.setMinBudget(minBudget);
        filter.setMaxBudget(maxBudget);
        filter.setStartDateFrom(startDateFrom);
        filter.setStartDateTo(startDateTo);
        filter.setEndDateFrom(endDateFrom);
        filter.setEndDateTo(endDateTo);

        return projectService.searchResponse(filter, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um projeto")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        return projectService.updateResponse(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status do projeto seguindo o fluxo permitido")
    public ProjectResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ProjectStatusUpdateRequest request) {
        return projectService.changeStatusResponse(id, request);
    }

    @PatchMapping("/{id}/members")
    @Operation(summary = "Atualiza os membros alocados no projeto")
    public ProjectResponse updateMembers(@PathVariable Long id, @Valid @RequestBody ProjectMembersUpdateRequest request) {
        return projectService.updateMembersResponse(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um projeto quando permitido pela regra de negócio")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/report")
    @Operation(summary = "Gera um relatório resumido do portfólio")
    public PortfolioReportResponse report() {
        return reportService.generate();
    }
}

