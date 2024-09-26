package org.socialculture.platform.member.oauth.naver.controller;

import org.socialculture.platform.member.oauth.naver.service.NaverClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
public class NaverClientController {
    private final NaverClientService naverClient;

    public NaverClientController(NaverClientService naverClient) {
        this.naverClient = naverClient;
    }

    /**
     * 네이버 API로 인가코드 요청 (테스트를 위한 코드로, 원래는 프론트 코드임)
     * @return 인가코드 요청 URL
     */
    @GetMapping("/naver")
    public String getNaverLoginURL() {
        String naverLoginURL = naverClient.getLoginURL();
        System.out.println(naverLoginURL);
        return naverLoginURL;
    }

    /**
     * 인가코드로 네이버 사용자 정보 요청
     * @param code 인가코드
     * @param state 상태코드
     */
    @GetMapping("/naver/callback")
    public void naverCallBack(@RequestParam String code, @RequestParam String state) {
        System.out.println("code: " + code);
        System.out.println("state: " + state);

        //인가코드로 액세스 토큰 요청
        String naverAccessToken = naverClient.getAccessToken(code, state);
        System.out.println("access token: " + naverAccessToken);
    }
}
