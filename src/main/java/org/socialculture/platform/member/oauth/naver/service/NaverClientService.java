package org.socialculture.platform.member.oauth.naver.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
}
