package com.kt.team06.member.service;

import com.kt.team06.member.dto.event.MemberSignedUpEvent;
import com.kt.team06.member.dto.request.MemberPasswordUpdateRequest;
import com.kt.team06.member.dto.request.MemberSignupRequest;
import com.kt.team06.member.dto.request.MemberUpdateRequest;
import com.kt.team06.member.dto.response.MemberIdResponse;
import com.kt.team06.member.entity.Member;
import com.kt.team06.member.repository.MemberRepository;
import com.kt.team06.member.global.util.PasswordUtil;
import com.kt.team06.member.service.kafka.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.common.protocol.types.Field.Bool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final KeycloakClientService keycloakClientService;
    private final KafkaProducerService kafkaProducerService;

    @Value("${kafka.topic.member-signed-up}")
    private String memberSignedUpTopic;

    @Override
    public MemberIdResponse signup(MemberSignupRequest request) {

        if (memberRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("존재하는 이메일입니다.");

        String uid = keycloakClientService.createUser(request);

        if (uid == null) {
            throw new IllegalArgumentException("keycloak 멤버 생성 오류");
        }

        Member newMember = memberRepository.save(
                MemberSignupRequest.toEntity(request, uid)
        );

        kafkaProducerService.send(memberSignedUpTopic, MemberSignedUpEvent.of(newMember));

        log.info(uid);

        return MemberIdResponse.of(newMember.getId());
    }

    @Override
    public MemberIdResponse withdraw(String memberId) {

        Member member = loadMember(memberId);

        String response = keycloakClientService.deleteUser(memberId);

        memberRepository.delete(member);

        log.info(response);

        return MemberIdResponse.of(member.getId());
    }

    @Override
    @Transactional
    public MemberIdResponse updateMember(String memberId, MemberUpdateRequest request) {

        keycloakClientService.updateUser(memberId, request);
        Member member = loadMember(memberId);
        member.update(request);

        return MemberIdResponse.of(member.getId());
    }

    @Override
    @Transactional
    public MemberIdResponse updatePassword(String memberId, MemberPasswordUpdateRequest request) {

        keycloakClientService.updatePassword(memberId, request);

        Member member = loadMember(memberId);
        if(!PasswordUtil.matches(request.oldPassword(), member.getPassword()))
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");

        member.updatePassword(
                PasswordUtil.encode(request.newPassword())
        );

        return MemberIdResponse.of(member.getId());
    }


    private Member loadMember(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }
}
