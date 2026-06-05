package com.portfolio.manager.dto.project;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ProjectMembersUpdateRequest {

    @NotEmpty
    private List<Long> memberIds;

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }
}

