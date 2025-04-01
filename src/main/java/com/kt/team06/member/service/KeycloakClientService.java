package com.kt.team06.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakClientService {

    private final WebClient.Builder webClientBuilder;

    public String getToken(String username, String password) {
        WebClient webClient = webClientBuilder
            .baseUrl("http://keycloak.keycloak.svc.cluster.local")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();

        log.info("TEST 2");
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", "admin-client");
        formData.add("username", username);
        formData.add("password", password);

        log.info("TEST 3");
        return webClient.post()
            .uri("/realms/master/protocol/openid-connect/token")
            .bodyValue(formData)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }
}
