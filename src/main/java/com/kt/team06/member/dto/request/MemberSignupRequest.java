package com.kt.team06.member.dto.request;

import com.kt.team06.member.entity.Member;
import com.kt.team06.member.global.util.PasswordUtil;

public record MemberSignupRequest(
        String email, String password, String name
) {

    public static Member toEntity(MemberSignupRequest memberSignupRequest) {
        return Member.builder()
                .email(memberSignupRequest.email())
                .password(PasswordUtil.encode(memberSignupRequest.password()))
                .name(memberSignupRequest.name())
                .build();
    }
}
