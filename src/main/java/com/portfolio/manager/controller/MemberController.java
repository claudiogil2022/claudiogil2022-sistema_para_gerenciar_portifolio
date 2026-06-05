package com.portfolio.manager.controller;

import com.portfolio.manager.domain.Member;
import com.portfolio.manager.dto.member.MemberCreateRequest;
import com.portfolio.manager.dto.member.MemberResponse;
import com.portfolio.manager.mapper.MemberMapper;
import com.portfolio.manager.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/external/members")
@Validated
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @Operation(summary = "Cria um membro na API mockada externa")
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody MemberCreateRequest request) {
        Member member = memberService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberMapper.toResponse(member));
    }

    @GetMapping
    @Operation(summary = "Lista os membros cadastrados na API mockada externa")
    public List<MemberResponse> list() {
        return memberService.findAll().stream().map(MemberMapper::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um membro pelo identificador")
    public MemberResponse findById(@PathVariable Long id) {
        return MemberMapper.toResponse(memberService.findById(id));
    }
}

