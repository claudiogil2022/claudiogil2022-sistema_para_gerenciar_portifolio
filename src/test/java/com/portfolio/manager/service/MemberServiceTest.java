package com.portfolio.manager.service;

import com.portfolio.manager.domain.Member;
import com.portfolio.manager.dto.member.MemberCreateRequest;
import com.portfolio.manager.exception.NotFoundException;
import com.portfolio.manager.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    @Test
    void shouldCreateMemberTrimmingNameAndRole() {
        MemberCreateRequest request = new MemberCreateRequest();
        request.setName("  Ana  ");
        request.setRole("  funcionário  ");

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Member created = memberService.create(request);

        assertEquals("Ana", created.getName());
        assertEquals("funcionário", created.getRole());
    }

    @Test
    void shouldFindMemberById() {
        Member member = new Member();
        member.setId(10L);
        member.setName("João");

        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));

        Member found = memberService.findById(10L);

        assertEquals(10L, found.getId());
        assertEquals("João", found.getName());
    }

    @Test
    void shouldThrowNotFoundWhenMemberDoesNotExist() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> memberService.findById(99L));
    }

    @Test
    void shouldListAllMembers() {
        Member memberA = new Member();
        memberA.setId(1L);
        Member memberB = new Member();
        memberB.setId(2L);

        when(memberRepository.findAll()).thenReturn(Arrays.asList(memberA, memberB));

        assertEquals(2, memberService.findAll().size());
    }

    @Test
    void shouldDetectEmployeeUsingNormalizedRole() {
        Member employee = new Member();
        employee.setRole("  FUNCIONÁRIO ");

        assertTrue(memberService.isEmployee(employee));
        assertFalse(memberService.isEmployee(null));

        Member contractor = new Member();
        contractor.setRole("terceiro");
        assertFalse(memberService.isEmployee(contractor));

        Member emptyRole = new Member();
        emptyRole.setRole("   ");
        assertFalse(memberService.isEmployee(emptyRole));
    }
}


