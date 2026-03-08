package pard.server.com.longkathon.config.webSocket.chatRoom;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pard.server.com.longkathon.config.jwt.token.CustomPrincipal;
import pard.server.com.longkathon.config.webSocket.dto.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    @PostMapping("/v1/chatRoom") //채팅방을 열어라.
    public ResponseEntity<ChatRoomResponse> enterChatRoom(@RequestBody CreateChatRoomRequest req,
                                                           @AuthenticationPrincipal CustomPrincipal principal){
        return new ResponseEntity<>(chatRoomService.createChatRoom(principal.userId(), req.getPartnerId()), HttpStatus.CREATED);
    }

    // 내 채팅방 목록 조회
    @GetMapping("/v1/chatRoom/my")
    public ResponseEntity<List<ChatRoomListResponse>> getMyChatRooms(
            @AuthenticationPrincipal CustomPrincipal principal) {
        return ResponseEntity.ok(chatRoomService.getMyChatRooms(principal.userId()));
    }

    // 특정 채팅방 상세 조회
    @GetMapping("/v1/chatRoom/{chatRoomId}")
    public ResponseEntity<ChatRoomDetailResponse> getChatRoomDetail(
            @PathVariable Long chatRoomId,
            @AuthenticationPrincipal CustomPrincipal principal) {
        return ResponseEntity.ok(chatRoomService.getChatRoomDetail(chatRoomId, principal.userId()));
    }
}
