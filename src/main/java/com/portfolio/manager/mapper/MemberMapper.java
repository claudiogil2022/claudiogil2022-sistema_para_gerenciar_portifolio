package com.portfolio.manager.mapper;

import com.portfolio.manager.domain.Member;
import com.portfolio.manager.dto.member.MemberResponse;

public final class MemberMapper {

    private MemberMapper() {
    }

    public static MemberResponse toResponse(Member member) {
        if (member == null) {
            return null;
        }
        return new MemberResponse(member.getId(), member.getName(), member.getRole());
    }
}

