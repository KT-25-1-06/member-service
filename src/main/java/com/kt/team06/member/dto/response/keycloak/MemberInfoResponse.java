package com.kt.team06.member.dto.response.keycloak;

public record MemberInfoResponse(
        String id,
        String username,
        String firstName,
        String lastName,
        String email,
        boolean emailVerified,
        long createdTimestamp,
        boolean enabled,
        boolean totp,
        String[] disableableCredentialTypes,
        String[] requiredActions,
        int notBefore,
        Access access
) {
    public record Access(
            boolean manageGroupMembership,
            boolean view,
            boolean mapRoles,
            boolean impersonate,
            boolean manage
    ) {}
}
