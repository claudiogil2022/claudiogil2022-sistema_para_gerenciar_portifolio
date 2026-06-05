package com.portfolio.manager.dto.member;

import jakarta.validation.constraints.NotBlank;

public class MemberCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

