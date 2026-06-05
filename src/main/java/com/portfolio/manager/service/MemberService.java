package com.portfolio.manager.service;

import com.portfolio.manager.domain.Member;
import com.portfolio.manager.domain.TextNormalizer;
import com.portfolio.manager.dto.member.MemberCreateRequest;
import com.portfolio.manager.exception.NotFoundException;
import com.portfolio.manager.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member create(MemberCreateRequest request) {
        Member member = new Member();
        member.setName(request.getName().trim());
        member.setRole(request.getRole().trim());
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Membro não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    public boolean isEmployee(Member member) {
        return member != null && "funcionario".equals(TextNormalizer.normalize(member.getRole()));
    }
}

