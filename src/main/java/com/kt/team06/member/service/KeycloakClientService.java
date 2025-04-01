package com.kt.team06.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.apache.kafka.common.message.LeaveGroupResponseData.MemberResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.kt.team06.member.dto.request.MemberPasswordUpdateRequest;
import com.kt.team06.member.dto.request.MemberSignupRequest;
import com.kt.team06.member.dto.request.MemberUpdateRequest;
import com.kt.team06.member.dto.request.keycloak.UserCreateRequest;
import com.kt.team06.member.dto.response.keycloak.AdminTokenResponse;
import com.kt.team06.member.dto.response.keycloak.MemberInfoResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakClientService {

    private final WebClient.Builder webClientBuilder;
    private String accessToken;
    private String refreshToken;
    private final String adminID = "user";
    private final String adminPassword = "test1234";


    public void getAccessToken() {
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
        try {
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
        } catch (WebClientResponseException e) {
            log.info(e.getMessage() + "Admin refresh 토큰 없거나 만료됨");
            try {
                getAccessToken(); // 새로운 액세스 토큰 발급
            } catch (WebClientResponseException lastException) {
                throw lastException;
            }
        }
    }

    public String createUser(MemberSignupRequest request) {
        String createdUID = null;

        try {
            refreshAdminToken();
            
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
                
                
            MemberInfoResponse memberInfo = getKeycloakMemberInfo(request.email());
            
            webClient.put()
                .uri(uriBuilder -> uriBuilder
                    .path("/admin/realms/team-06/users/" + memberInfo.id() + "/send-verify-email")
                    // .queryParam("client_id", "login-client")
                    // .queryParam("redirect_uri", "https://team-06.kt.com/verify-email-success")
                    .build())
                .retrieve()
                .bodyToMono(Void.class)
                .block();

            return memberInfo.id();
        } catch (WebClientResponseException e) {
            log.info(e.getMessage());
            return null;
        }
    }

    private MemberInfoResponse getKeycloakMemberInfo(String email) {
        WebClient webClient = webClientBuilder
            .baseUrl("http://keycloak.keycloak.svc.cluster.local")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .build();

        try {
            MemberInfoResponse[] usersArray = webClient.get()
                .uri("/admin/realms/team-06/users?email="+email)
                .retrieve()
                .bodyToMono(MemberInfoResponse[].class)
                .block();
            return usersArray[0];
        } catch (WebClientResponseException e) {
            log.info(e.getMessage());
            return null;
        }
    }

    public String deleteUser(String id) {
        
        try {
            refreshAdminToken();
            WebClient webClient = webClientBuilder
                .baseUrl("http://keycloak.keycloak.svc.cluster.local")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();
            
            webClient.delete()
                .uri("/admin/realms/team-06/users/"+id)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            
            return "유저 삭제됨";
        } catch (WebClientResponseException e) {
            return e.getMessage();
        }
    }

    public String updateUser(String id, MemberUpdateRequest request) {
        try {
            refreshAdminToken();

            WebClient webClient = webClientBuilder
                .baseUrl("http://keycloak.keycloak.svc.cluster.local")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();

            // 사용자 정보 업데이트 요청 객체 생성
            MemberUpdateRequest userRequest = new MemberUpdateRequest(
                request.username()
            );

            webClient.put()
                .uri("/admin/realms/team-06/users/" + id)
                .bodyValue(userRequest)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            
            return "유저 정보 업데이트됨";

        } catch (WebClientResponseException e) {
            return e.getMessage();
        }
    }

    public String updatePassword(String id, MemberPasswordUpdateRequest request) {
        try {
            refreshAdminToken();
            WebClient webClient = webClientBuilder
                .baseUrl("http://keycloak.keycloak.svc.cluster.local")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();

            // 비밀번호 업데이트 요청 객체 생성
            UserCreateRequest.Credential credential = new UserCreateRequest.Credential(
                "password",
                request.newPassword(),
                false
            );

            webClient.put()
                .uri("/admin/realms/team-06/users/" + id + "/reset-password")
                .bodyValue(credential)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
            
            return "비밀번호 업데이트됨";
            
        } catch (WebClientResponseException e) {
            return e.getMessage();
        }
    }
}
