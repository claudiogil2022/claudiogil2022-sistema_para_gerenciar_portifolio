package com.portfolio.manager.repository;

import com.portfolio.manager.domain.Project;
import com.portfolio.manager.domain.ProjectStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    public static Specification<Project> withName(String name) {
        return (root, query, cb) -> name == null || name.trim().isEmpty()
                ? cb.conjunction()
                : cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
    }

    public static Specification<Project> withStatus(ProjectStatus status) {
        return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Project> withManagerName(String managerName) {
        return (root, query, cb) -> {
            if (managerName == null || managerName.trim().isEmpty()) {
                return cb.conjunction();
            }
            query.distinct(true);
            return cb.like(cb.lower(root.join("manager", JoinType.LEFT).get("name")), "%" + managerName.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Project> withMinBudget(BigDecimal minBudget) {
        return (root, query, cb) -> minBudget == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("budget"), minBudget);
    }

    public static Specification<Project> withMaxBudget(BigDecimal maxBudget) {
        return (root, query, cb) -> maxBudget == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("budget"), maxBudget);
    }

    public static Specification<Project> withStartDateFrom(LocalDate startDateFrom) {
        return (root, query, cb) -> startDateFrom == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom);
    }

    public static Specification<Project> withStartDateTo(LocalDate startDateTo) {
        return (root, query, cb) -> startDateTo == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("startDate"), startDateTo);
    }

    public static Specification<Project> withEndDateFrom(LocalDate endDateFrom) {
        return (root, query, cb) -> endDateFrom == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("plannedEndDate"), endDateFrom);
    }

    public static Specification<Project> withEndDateTo(LocalDate endDateTo) {
        return (root, query, cb) -> endDateTo == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("plannedEndDate"), endDateTo);
    }
}

