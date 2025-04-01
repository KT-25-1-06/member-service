package com.kt.team06.member.dto.event;

import com.kt.team06.member.entity.Member;

public record MemberSignedUpEvent(
        String memberId,
        String email,
        String name
) {

    public static MemberSignedUpEvent of(Member member) {
        return new MemberSignedUpEvent(
                member.getId(),
                member.getEmail(),
                member.getUsername()
        );
    }
}