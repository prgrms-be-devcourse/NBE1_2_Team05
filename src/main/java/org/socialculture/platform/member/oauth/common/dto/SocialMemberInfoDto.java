package org.socialculture.platform.member.oauth.common.dto;

/**
 * 소셜 사용자가 회원가입중 닉네임을 정할때 사용
 * @param name
 */
public record SocialMemberInfoDto(
        String name
) {
}
