package com.kt.team06.member.entity;

import com.kt.team06.member.dto.request.MemberUpdateRequest;
import com.kt.team06.member.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private String password;

    private String username;
    
    private String firstName;

    private String lastName;

    public void update(MemberUpdateRequest request) {
        this.username = request.username();
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}
