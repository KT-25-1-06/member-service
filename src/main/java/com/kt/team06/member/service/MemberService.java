package com.kt.team06.member.service;

import com.kt.team06.member.dto.request.MemberPasswordUpdateRequest;
import com.kt.team06.member.dto.request.MemberSignupRequest;
import com.kt.team06.member.dto.request.MemberUpdateRequest;
import com.kt.team06.member.dto.response.MemberIdResponse;

public interface MemberService {

    MemberIdResponse signup(MemberSignupRequest request);
    MemberIdResponse withdraw(Long memberId);
    MemberIdResponse updateMember(Long memberId, MemberUpdateRequest request);
    MemberIdResponse updatePassword(Long memberId, MemberPasswordUpdateRequest request);

}
