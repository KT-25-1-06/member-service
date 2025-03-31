package com.kt.team06.member.dto.request;

public record MemberPasswordUpdateRequest(
        String oldPassword, String newPassword
) {
}
