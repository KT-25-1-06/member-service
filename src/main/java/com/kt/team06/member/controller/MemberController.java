package com.kt.team06.member.controller;

import com.kt.team06.member.dto.request.MemberPasswordUpdateRequest;
import com.kt.team06.member.dto.request.MemberSignupRequest;
import com.kt.team06.member.dto.request.MemberUpdateRequest;
import com.kt.team06.member.dto.response.MemberIdResponse;
import com.kt.team06.member.global.ApiResponse;
import com.kt.team06.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<MemberIdResponse>> signup(@RequestBody MemberSignupRequest request) {
        return ResponseEntity.ok(ApiResponse.success(memberService.signup(request)));
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<ApiResponse<MemberIdResponse>> deleteMember(
            @RequestHeader("x-id") String memberId) {
        return ResponseEntity.ok(ApiResponse.success(memberService.withdraw(memberId)));
    }

    @PutMapping("/")
    public ResponseEntity<ApiResponse<MemberIdResponse>> updateMember(
            @RequestHeader("x-id") String memberId, @RequestBody MemberUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.updateMember(memberId, request)));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<MemberIdResponse>> updatePassword(
            @RequestHeader("x-id") String memberId, @RequestBody MemberPasswordUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(memberService.updatePassword(memberId, request)));
    }

    @GetMapping("/verify-email-success")
    public ResponseEntity<ApiResponse<String>> updatePassword() {
        return ResponseEntity.ok(ApiResponse.success("verify-email-success"));
    }
}