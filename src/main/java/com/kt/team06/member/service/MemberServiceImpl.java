package com.kt.team06.member.service;

import com.kt.team06.member.dto.request.MemberPasswordUpdateRequest;
import com.kt.team06.member.dto.request.MemberSignupRequest;
import com.kt.team06.member.dto.request.MemberUpdateRequest;
import com.kt.team06.member.dto.response.MemberIdResponse;
import com.kt.team06.member.entity.Member;
import com.kt.team06.member.repository.MemberRepository;
import com.kt.team06.member.global.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.common.protocol.types.Field.Bool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final KeycloakClientService keycloakClientService;

    @Override
    public MemberIdResponse signup(MemberSignupRequest request) {

        if (memberRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("존재하는 이메일입니다.");

        String response = keycloakClientService.createUser(request);

        Member newMember = memberRepository.save(
                MemberSignupRequest.toEntity(request)
        );

        log.info(response);

        return MemberIdResponse.of(newMember.getId());
    }

    @Override
    public MemberIdResponse withdraw(String memberId) {

        Member member = loadMember(memberId);

        memberRepository.delete(member);

        return MemberIdResponse.of(member.getId());
    }

    @Override
    @Transactional
    public MemberIdResponse updateMember(Long memberId, MemberUpdateRequest request) {

        Member member = loadMember(memberId);
        member.update(request);

        return MemberIdResponse.of(member.getId());
    }

    @Override
    @Transactional
    public MemberIdResponse updatePassword(Long memberId, MemberPasswordUpdateRequest request) {

        Member member = loadMember(memberId);
        if(!PasswordUtil.matches(request.oldPassword(), member.getPassword()))
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");

        member.updatePassword(
                PasswordUtil.encode(request.newPassword())
        );

        return MemberIdResponse.of(member.getId());
    }


    private Member loadMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }
}
