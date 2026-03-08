package pard.server.com.longkathon.config.webSocket.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import pard.server.com.longkathon.config.webSocket.chatRoom.ChatRoom;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatRoomResponse {
    private Long chatRoomId;
    private Long userId;
    private Long partnerId;
    private List<ChatMessageResponse> messages;

    public static ChatRoomResponse fromEntity(ChatRoom chatRoom, List<ChatMessageResponse> messages) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getUserId(),
                chatRoom.getPartnerId(),
                messages
        );
    }
}