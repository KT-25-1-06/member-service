package com.kt.team06.member.dto.response;

public record MemberIdResponse(
        String memberId
) {

    public static MemberIdResponse of(String memberId) {
        return new MemberIdResponse(memberId);
    }
}
