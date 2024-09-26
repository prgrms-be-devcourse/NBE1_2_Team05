package org.socialculture.platform.member.oauth.naver.dto;

import lombok.Builder;

@Builder
public record NaverResponseDTO(String name, String email) {
    public static NaverResponseDTO of(String name, String email) {
        return NaverResponseDTO.builder()
                .name(name)
                .email(email)
                .build();
    }
}
