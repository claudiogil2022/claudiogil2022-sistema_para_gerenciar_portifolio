package com.portfolio.manager.mapper;

import com.portfolio.manager.domain.Member;
import com.portfolio.manager.domain.Project;
import com.portfolio.manager.dto.project.ProjectResponse;
import com.portfolio.manager.service.ProjectRiskService;

import java.util.ArrayList;
import java.util.List;

public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setStartDate(project.getStartDate());
        response.setPlannedEndDate(project.getPlannedEndDate());
        response.setRealEndDate(project.getRealEndDate());
        response.setBudget(project.getBudget());
        response.setDescription(project.getDescription());
        response.setManager(MemberMapper.toResponse(project.getManager()));
        response.setStatus(project.getStatus());
        response.setRiskLevel(ProjectRiskService.calculateRisk(project.getBudget(), project.getStartDate(), project.getPlannedEndDate()));
        response.setMembers(toMemberResponses(project.getMembers()));
        return response;
    }

    private static List<com.portfolio.manager.dto.member.MemberResponse> toMemberResponses(java.util.Set<Member> members) {
        List<com.portfolio.manager.dto.member.MemberResponse> responses = new ArrayList<>();
        if (members == null) {
            return responses;
        }
        for (Member member : members) {
            responses.add(MemberMapper.toResponse(member));
        }
        return responses;
    }
}

