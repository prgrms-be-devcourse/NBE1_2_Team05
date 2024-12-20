package org.socialculture.platform.chat.controller;

import lombok.RequiredArgsConstructor;
import org.socialculture.platform.chat.dto.ChatMessageDto;
import org.socialculture.platform.chat.dto.request.ChatMessageRequestDto;
import org.socialculture.platform.chat.dto.request.ChatRoomRequestDto;
import org.socialculture.platform.chat.dto.response.ChatMessageResponseDto;
import org.socialculture.platform.chat.dto.response.ChatRoomResponseDto;
import org.socialculture.platform.chat.service.ChatService;
import org.socialculture.platform.global.apiResponse.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chats/room")
public class ChatController {

    private final ChatService chatService;

    // 새로운 채팅방 생성
    @PostMapping("/{performanceId}")
    public ResponseEntity<ApiResponse<ChatRoomResponseDto>> createChatRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long performanceId) {
        return ApiResponse.onSuccess(chatService.createChatRoom(userDetails.getUsername(), performanceId));
    }

    // 채팅 메시지 전송
    @PostMapping("/{chatRoomId}/message")
    public ResponseEntity<ApiResponse<ChatMessageResponseDto>> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long chatRoomId,
            @RequestBody ChatMessageRequestDto chatMessageRequestDto) {
        ChatMessageDto chatMessageDto = ChatMessageDto.of(chatRoomId,
                userDetails.getUsername(),
                chatMessageRequestDto.messageContent());
        return ApiResponse.onSuccess(chatService.saveMessage(chatMessageDto));
    }

    // 특정 채팅방의 메시지 목록 조회
    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponseDto>>> getMessages(@PathVariable Long chatRoomId) {
        return ApiResponse.onSuccess(chatService.getMessagesByChatRoom(chatRoomId));
    }

    // 사용자가 참여한 채팅방 목록 조회
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ChatRoomResponseDto>>> getChatRooms(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "isManager", defaultValue = "false") boolean isManager) {
        return ApiResponse.onSuccess(chatService.getChatRoomsByMember(userDetails.getUsername(), isManager));
    }
}
