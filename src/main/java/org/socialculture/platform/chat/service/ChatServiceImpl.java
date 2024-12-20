package org.socialculture.platform.chat.service;

import lombok.RequiredArgsConstructor;
import org.socialculture.platform.chat.dto.ChatMessageDto;
import org.socialculture.platform.chat.dto.response.ChatMessageResponseDto;
import org.socialculture.platform.chat.dto.response.ChatRoomResponseDto;
import org.socialculture.platform.chat.entity.ChatMessageEntity;
import org.socialculture.platform.chat.entity.ChatRoomEntity;
import org.socialculture.platform.chat.repository.ChatMessageRepository;
import org.socialculture.platform.chat.repository.ChatRoomRepository;
import org.socialculture.platform.global.apiResponse.exception.ErrorStatus;
import org.socialculture.platform.global.apiResponse.exception.GeneralException;
import org.socialculture.platform.member.entity.MemberEntity;
import org.socialculture.platform.member.repository.MemberRepository;
import org.socialculture.platform.performance.entity.PerformanceEntity;
import org.socialculture.platform.performance.repository.PerformanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final PerformanceRepository performanceRepository;

    @Override
    public ChatRoomResponseDto createChatRoom(String email, Long performanceId) {
        PerformanceEntity performanceEntity = performanceRepository.findById(performanceId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PERFORMANCE_NOT_FOUND));
        MemberEntity memberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        MemberEntity managerEntity = memberRepository.findById(performanceEntity.getMember().getMemberId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 채팅방 중복 확인
        Optional<ChatRoomEntity> existingChatRoom = chatRoomRepository.getChatRoomByPerformanceIdAndMemberId(
                performanceEntity.getPerformanceId(),
                memberEntity.getMemberId()
        );

        if (existingChatRoom.isPresent()) {
            ChatRoomEntity existingRoom = existingChatRoom.get();

            return ChatRoomResponseDto.fromEntity(existingRoom, null, null);
        }

        // 새 채팅방 생성
        ChatRoomEntity chatRoomEntity = ChatRoomEntity.builder()
                .performance(performanceEntity)
                .member(memberEntity)
                .manager(managerEntity)
                .build();

        ChatRoomEntity savedChatRoom = chatRoomRepository.save(chatRoomEntity);

        // ChatRoomEntity -> ChatRoomResponseDto 변환 후 반환
        return ChatRoomResponseDto.fromEntity(savedChatRoom, null, null);
    }

    @Override
    public ChatMessageResponseDto saveMessage(ChatMessageDto chatMessageDto) {
        ChatRoomEntity chatRoomEntity = chatRoomRepository.findById(chatMessageDto.getChatRoomId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHAT_ROOM_NOT_FOUND));
        MemberEntity senderEntity = memberRepository.findByName(chatMessageDto.getSenderName())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        ChatMessageEntity message = ChatMessageEntity.builder()
                .chatRoomEntity(chatRoomEntity)
                .sender(senderEntity)
                .messageContent(chatMessageDto.getMessage())
                .isRead(false)
                .build();

        ChatMessageEntity savedMessage = chatMessageRepository.save(message);

        // ChatMessageEntity -> ChatMessageResponseDto 변환 후 반환
        return ChatMessageResponseDto.fromEntity(savedMessage);
    }

    @Override
    public List<ChatMessageResponseDto> getMessagesByChatRoom(Long chatRoomId) {
        // 엔티티 목록을 DTO로 변환
        return chatMessageRepository.getMessagesByChatRoomId(chatRoomId).stream()
                .map(ChatMessageResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatRoomResponseDto> getChatRoomsByMember(String email, boolean isManager) {
        // 이메일로 사용자가 참여한 채팅방 목록을 조회
        return chatRoomRepository.getChatRoomsByEmail(email, isManager).stream()
                .map(chatRoomEntity -> {
                    // 각 채팅방의 마지막 메시지와 시간 경과 조회
                    Map<String, Object> lastMessageInfo = chatMessageRepository.findLastMessageAndTimeAgoForChatRoom(chatRoomEntity.getChatRoomId());

                    // lastMessage와 timeAgo 값을 추출 (null일 경우 기본값 설정)
                    String lastMessage = lastMessageInfo != null ? (String) lastMessageInfo.get("lastMessage") : "";
                    String timeAgo = lastMessageInfo != null ? (String) lastMessageInfo.get("timeAgo") : "";

                    // ChatRoomEntity와 마지막 메시지 정보를 사용해 ChatRoomResponseDto 생성
                    return ChatRoomResponseDto.fromEntity(chatRoomEntity, lastMessage, timeAgo);
                })
                .collect(Collectors.toList());
    }
}