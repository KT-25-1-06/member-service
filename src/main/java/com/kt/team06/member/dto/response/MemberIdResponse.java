package com.kt.team06.member.dto.response;

public record MemberIdResponse(
        Long memberId
) {

    public static MemberIdResponse of(Long memberId) {
        return new MemberIdResponse(memberId);
    }
}
