package com.kt.team06.member.repository;

import com.kt.team06.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {
    Boolean existsByEmail(String email);
}
