package pard.server.com.longkathon.config.webSocket.chatRoom;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pard.server.com.longkathon.config.jwt.token.CustomPrincipal;
import pard.server.com.longkathon.config.webSocket.dto.ChatRoomResponse;
import pard.server.com.longkathon.config.webSocket.dto.CreateChatRoomRequest;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    @PostMapping("/v1/chatRoom") //채팅바을 열어라.
    public ResponseEntity<ChatRoomResponse> enterChatRoom(@RequestBody CreateChatRoomRequest req,
                                                           @AuthenticationPrincipal CustomPrincipal principal){
        return new ResponseEntity<>(chatRoomService.createChatRoom(principal.userId(), req.getSellerId()), HttpStatus.CREATED);
    }
}
