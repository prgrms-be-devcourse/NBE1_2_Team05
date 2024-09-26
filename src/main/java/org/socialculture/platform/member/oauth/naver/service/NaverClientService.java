package org.socialculture.platform.member.oauth.naver.service;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

@Service
public class NaverClientService {
    @Value("${naver.client_id}")
    private String clientId;

    @Value("${naver.redirect_uri}")
    private String redirectURI;

    @Value("${naver.client_secret}")
    private String clientSecret;

    public String getLoginURL() {
        return "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=" +
                clientId +
                "&redirect_uri=" +
                redirectURI;
    }

    public String getAccessToken(String code, String state) {
        String reqUrl = "https://nid.naver.com/oauth2.0/token";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(reqUrl, HttpMethod.POST, tokenRequest, String.class);

        JsonObject asJsonObject = JsonParser.parseString(Objects.requireNonNull(response.getBody())).getAsJsonObject();
        return asJsonObject.get("access_token").getAsString();
    }
}
