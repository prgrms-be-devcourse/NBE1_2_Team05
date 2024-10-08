package org.socialculture.platform.member.oauth.naver.service;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.socialculture.platform.member.entity.SocialProvider;
import org.socialculture.platform.member.oauth.common.dto.SocialMemberCheckDto;
import org.socialculture.platform.member.oauth.common.service.SocialClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * 네이버 OAuth 인증을 처리하고 사용자 정보를 조회하는 서비스 클래스
 * @author 김연수
 */
@Service
@Slf4j
public class NaverClient implements SocialClient {
    private final String clientId;
    private final String redirectURI;
    private final String clientSecret;
    private final RestTemplate restTemplate;

    public NaverClient(
            @Value("${spring.naver.client_id}") String clientId,
            @Value("${spring.naver.redirect_uri}") String redirectURI,
            @Value("${spring.naver.client_secret}") String clientSecret,
            RestTemplate restTemplate) {
        this.clientId = clientId;
        this.redirectURI = redirectURI;
        this.clientSecret = clientSecret;
        this.restTemplate = restTemplate;
    }

    /**
     * 인가코드로 네이버 API에 액세스 토큰 요청
     * @param code 인가코드
     * @param state 상태코드
     * @return 액세스 토큰
     */
    @Override
    public String getAccessToken(String code, String state) {
        String reqUrl = "https://nid.naver.com/oauth2.0/token";

//        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(reqUrl,
                HttpMethod.POST,
                tokenRequest,
                String.class);

        JsonObject asJsonObject = JsonParser
                .parseString(Objects.requireNonNull(response.getBody()))
                .getAsJsonObject();

        return asJsonObject.get("access_token").getAsString();
    }

    /**
     * 액세스 토큰으로 네이버 사용자 정보 요청
     * @param accessToken
     * @return 이름, 이메일
     */
    @Override
    public SocialMemberCheckDto getMemberInfo(String accessToken) {
        String reqUrl = "https://openapi.naver.com/v1/nid/me";

//        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> memberInfoRequest = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(reqUrl,
                HttpMethod.POST,
                memberInfoRequest,
                String.class);
        log.info("response : {}", response);

        // JSON 파싱
        JsonObject jsonObject = JsonParser
                .parseString(Objects.requireNonNull(response.getBody()))
                .getAsJsonObject();
        JsonObject responseObject = jsonObject.getAsJsonObject("response");

        String email = responseObject.get("email").getAsString();
        String providerId = responseObject.get("id").getAsString();

        // NaverEntity 생성 후 반환
        return SocialMemberCheckDto.create(email,providerId, SocialProvider.NAVER);
    }
}
