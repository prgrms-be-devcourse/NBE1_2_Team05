package org.socialculture.platform.chat.dto.response;

import lombok.Builder;
import org.socialculture.platform.chat.entity.ChatRoomEntity;

@Builder
public record ChatRoomResponseDto(
        Long chatRoomId,
        Long performanceId,
        Long memberId,
        Long managerId,
        String title,       // 공연 이름
        String imageUrl,    // 공연 이미지(url)
        String lastMessage, // 마지막 메세지
        String timeAgo      // 마지막 메세지를 기준으로 지난 시간
) {
    // fromEntity 메서드 - lastMessage와 timeAgo를 인자로 받아서 사용
    public static ChatRoomResponseDto fromEntity(ChatRoomEntity chatRoomEntity, String lastMessage, String timeAgo) {
        return ChatRoomResponseDto.builder()
                .chatRoomId(chatRoomEntity.getChatRoomId())
                .performanceId(chatRoomEntity.getPerformance().getPerformanceId())
                .memberId(chatRoomEntity.getMember().getMemberId())
                .managerId(chatRoomEntity.getManager().getMemberId())
                .title(chatRoomEntity.getPerformance().getTitle())
                .imageUrl(chatRoomEntity.getPerformance().getImageUrl())
                .lastMessage(lastMessage) // 최신 메시지 내용
                .timeAgo(timeAgo)         // 최신 메시지 시간 경과
                .build();
    }
}
