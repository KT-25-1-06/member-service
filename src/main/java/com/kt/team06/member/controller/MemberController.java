package com.kt.team06.member.controller;

import com.kt.team06.member.dto.request.MemberPasswordUpdateRequest;
import com.kt.team06.member.dto.request.MemberSignupRequest;
import com.kt.team06.member.dto.request.MemberUpdateRequest;
import com.kt.team06.member.dto.response.MemberIdResponse;
import com.kt.team06.member.global.ApiResponse;
import com.kt.team06.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberIdResponse>> signup(@RequestBody MemberSignupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(memberService.signup(request)));
    }

    @DeleteMapping("/{memberId}/withdraw")
    public ResponseEntity<ApiResponse<MemberIdResponse>> deleteMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.withdraw(memberId)));
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<ApiResponse<MemberIdResponse>> updateMember(
            @PathVariable Long memberId, @RequestBody MemberUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.updateMember(memberId, request)));
    }

    @PutMapping("/{memberId}/password")
    public ResponseEntity<ApiResponse<MemberIdResponse>> updatePassword(
            @PathVariable Long memberId, @RequestBody MemberPasswordUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.updatePassword(memberId, request)));
    }
}