package com.kt.team06.member.dto.request.keycloak;

public record UserCreateRequest(
        String username,
        String email,
        boolean enabled,
        String firstName,
        String lastName,
        Credential[] credentials
) {
    public record Credential(
            String type,
            String value,
            boolean temporary
    ) {}
}
