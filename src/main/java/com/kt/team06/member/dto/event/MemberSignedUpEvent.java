package com.kt.team06.member.dto.event;

import com.kt.team06.member.entity.Member;

public record MemberSignedUpEvent(
        String memberId,
        String email,
        String name
) {

    public static MemberSignedUpEvent of(Member member) {
        return new MemberSignedUpEvent(
                member.getEmail(), // 임시로 email -> id 타입 변경 수정
                member.getEmail(),
                member.getName()
        );
    }
}