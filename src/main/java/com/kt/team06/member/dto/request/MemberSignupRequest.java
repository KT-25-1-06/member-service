package com.kt.team06.member.dto.request;

import com.kt.team06.member.entity.Member;
import com.kt.team06.member.global.util.PasswordUtil;

public record MemberSignupRequest(
        String email, String password, String username, String firstName, String lastName
) {

    public static Member toEntity(MemberSignupRequest memberSignupRequest, String id) {
        return Member.builder()
                .id(id)
                .email(memberSignupRequest.email())
                .password(PasswordUtil.encode(memberSignupRequest.password()))
                .username(memberSignupRequest.username())
                .firstName(memberSignupRequest.firstName())
                .lastName(memberSignupRequest.lastName())
                .build();
    }
}
