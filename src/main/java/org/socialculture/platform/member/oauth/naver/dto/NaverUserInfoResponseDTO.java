package org.socialculture.platform.member.oauth.naver.dto;

import lombok.Builder;

@Builder
public record NaverUserInfoResponseDTO(String name, String email) {
    public static NaverUserInfoResponseDTO of(String name, String email) {
        return NaverUserInfoResponseDTO.builder()
                .name(name)
                .email(email)
                .build();
    }
}
