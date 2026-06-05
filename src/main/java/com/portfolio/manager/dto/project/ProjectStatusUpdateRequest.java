package com.portfolio.manager.dto.project;

import com.portfolio.manager.domain.ProjectStatus;

import jakarta.validation.constraints.NotNull;

public class ProjectStatusUpdateRequest {

    @NotNull
    private ProjectStatus status;

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}

