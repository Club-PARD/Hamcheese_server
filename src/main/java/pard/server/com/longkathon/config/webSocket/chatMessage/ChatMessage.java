package pard.server.com.longkathon.config.webSocket.chatMessage;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pard.server.com.longkathon.BaseEntity.BaseEntity;
import pard.server.com.longkathon.config.webSocket.chatRoom.ChatRoom;

@Entity
@Table(name="ChatMessage")
@Getter
@NoArgsConstructor
public class ChatMessage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "chat_room_id")
    private Long chatRoomId;

    private Long senderId;

    private String content;

    public ChatMessage(ChatRoom chatRoom, Long senderId, String content) {
        this.chatRoomId = chatRoom.getId();
        this.senderId = senderId;
        this.content = content;
    }
}