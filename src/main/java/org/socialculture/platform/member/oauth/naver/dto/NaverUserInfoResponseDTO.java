package org.socialculture.platform.member.oauth.naver.dto;

import lombok.Builder;

@Builder
public record NaverUserInfoResponseDTO(String providerId, String email) {
    public static NaverUserInfoResponseDTO of(String providerId, String email) {
        return NaverUserInfoResponseDTO.builder()
                .providerId(providerId)
                .email(email)
                .build();
    }
}
