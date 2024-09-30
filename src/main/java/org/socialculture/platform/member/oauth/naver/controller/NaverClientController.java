package org.socialculture.platform.member.oauth.naver.controller;

import lombok.extern.slf4j.Slf4j;
import org.socialculture.platform.member.oauth.naver.dto.NaverUserInfoResponseDTO;
import org.socialculture.platform.member.oauth.naver.service.NaverClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 네이버 OAuth 인가코드를 받아온 후 콜백을 처리하고, 사용자 정보를 조회하는 컨트롤러 클래스
 * @author 김연수
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/members")
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

        //액세스 토큰으로 사용자 정보 요청
        NaverUserInfoResponseDTO naverMemberInfo = naverClient.getMemberInfo(naverAccessToken);
//        System.out.println(naverMemberInfo);
        log.info("네이버 사용자 정보 = {}", naverMemberInfo);
    }
}
