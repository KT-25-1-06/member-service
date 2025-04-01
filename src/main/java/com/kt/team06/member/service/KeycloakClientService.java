package com.kt.team06.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.common.message.LeaveGroupResponseData.MemberResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.kt.team06.member.dto.request.MemberSignupRequest;
import com.kt.team06.member.dto.request.keycloak.UserCreateRequest;
import com.kt.team06.member.dto.response.keycloak.AdminTokenResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakClientService {

    private final WebClient.Builder webClientBuilder;
    private String accessToken;
    private String refreshToken;
    private final String adminID = "user";
    private final String adminPassword = "test1234";

    public void getAdminToken() {
        WebClient webClient = webClientBuilder
            .baseUrl("http://keycloak.keycloak.svc.cluster.local")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", "admin-cli");
        formData.add("username", adminID);
        formData.add("password", adminPassword);

        AdminTokenResponse response = webClient.post()
            .uri("/realms/master/protocol/openid-connect/token")
            .bodyValue(formData)
            .retrieve()
            .bodyToMono(AdminTokenResponse.class)
            .block();
        accessToken = response.access_token();
        refreshToken = response.refresh_token();
        log.info("Admin access 토큰 발급");
    }

    public void refreshAdminToken() {
        WebClient webClient = webClientBuilder
            .baseUrl("http://keycloak.keycloak.svc.cluster.local")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", "admin-cli");
        formData.add("refresh_token", refreshToken);

        AdminTokenResponse response = webClient.post()
            .uri("/realms/master/protocol/openid-connect/token")
            .bodyValue(formData)
            .retrieve()
            .bodyToMono(AdminTokenResponse.class)
            .block();
        accessToken = response.access_token();
        refreshToken = response.refresh_token();
    }

    public String createUser(MemberSignupRequest request) {
        try {
            executeCreateUser(request);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 401) { // 액세스 토큰 만료
                try {
                    log.info(e.getMessage() + "Admin access 토큰 없거나 만료됨");
                    refreshAdminToken(); // 리프레시 토큰으로 재발급 시도
                    executeCreateUser(request);
                } catch (WebClientResponseException refreshException) {
                    log.info(e.getMessage() + "Admin refresh 토큰 없거나 만료됨");
                    try {
                        getAdminToken(); // 새로운 액세스 토큰 발급
                        executeCreateUser(request);
                    } catch (WebClientResponseException lastException) {
                        return lastException.getMessage();
                    }
                }
            } else if (e.getStatusCode().value() == 409) {
                log.info(e.getMessage());
                return e.getMessage();
            }
            
        }
        return "유저 생성됨";
    }

    private void executeCreateUser(MemberSignupRequest request) {
        WebClient webClient = webClientBuilder
            .baseUrl("http://keycloak.keycloak.svc.cluster.local")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .build();

        UserCreateRequest.Credential credential = new UserCreateRequest.Credential(
            "password",
            request.password(),
            false
        );

        UserCreateRequest userRequest = new UserCreateRequest(
            request.username(),
            request.email(),
            true,
            request.firstName(),
            request.lastName(),
            new UserCreateRequest.Credential[]{credential}
        );

        webClient.post()
            .uri("/admin/realms/team-06/users")
            .bodyValue(userRequest)
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    }

    // public String deleteUser(MemberResponse) {
    //     try {
    //         executeCreateUser(request);
    //     } catch (WebClientResponseException e) {
    //         if (e.getStatusCode().value() == 401) { // 액세스 토큰 만료
    //             try {
    //                 log.info(e.getMessage() + "Admin access 토큰 없거나 만료됨");
    //                 refreshAdminToken(); // 리프레시 토큰으로 재발급 시도
    //                 executeCreateUser(request);
    //             } catch (WebClientResponseException refreshException) {
    //                 log.info(e.getMessage() + "Admin refresh 토큰 없거나 만료됨");
    //                 try {
    //                     getAdminToken(); // 새로운 액세스 토큰 발급
    //                     executeCreateUser(request);
    //                 } catch (WebClientResponseException lastException) {
    //                     return lastException.getMessage();
    //                 }
    //             }
    //         } else if (e.getStatusCode().value() == 409) {
    //             log.info(e.getMessage());
    //             return e.getMessage();
    //         }
            
    //     }
    //     return "유저 생성됨";
    // }
}
