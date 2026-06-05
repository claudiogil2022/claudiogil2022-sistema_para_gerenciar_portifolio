package com.portfolio.manager.domain;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade JPA que representa um Projeto no portfólio.
 * 
 * Relacionamentos:
 * - Manager (ManyToOne): cada projeto tem exatamente 1 gerente (funcionário responsável)
 * - Members (ManyToMany): cada projeto pode ter múltiplos membros alocados
 * 
 * Decisões de design:
 * - LAZY loading em relacionamentos: melhora performance em consultas list/search
 * - HashSet para members: garante unicidade e previne alocações duplicadas
 * - Auditoria com CreationTimestamp: permite rastrear quando projeto foi criado
 * - BigDecimal para budget: evita problemas de arredondamento com valores monetários
 * 
 * TODO: Adicionar versioning (Optimistic Lock) para concorrência
 * TODO: Adicionar índices em name e status para melhorar buscas
 */
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "planned_end_date", nullable = false)
    private LocalDate plannedEndDate;

    @Column(name = "real_end_date")
    private LocalDate realEndDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal budget;

    @Column(length = 2000)
    private String description;

    // Relacionamento com gerente responsável (obrigatório)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private Member manager;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    // Relacionamento com membros alocados (pode ser múltiplo)
    // LAZY loading para não trazer membros desnecessariamente em consultas list
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id"))
    private Set<Member> members = new HashSet<Member>();

    // Auditoria: timestamp de criação do projeto
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

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

    public Member getManager() {
        return manager;
    }

    public void setManager(Member manager) {
        this.manager = manager;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Set<Member> getMembers() {
        return members;
    }

    public void setMembers(Set<Member> members) {
        this.members = members;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

