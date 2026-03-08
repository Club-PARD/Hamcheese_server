package pard.server.com.longkathon.config.webSocket.chatRoom;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pard.server.com.longkathon.BaseEntity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "ChatRoom",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chat_room_user_seller",
                        columnNames = {"userId", "sellerId"}
                )
        }
)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(name = "sellerId")  // DB 컬럼명은 sellerId 유지
    private Long partnerId;     // 코드에서는 partnerId 사용

    public ChatRoom(Long userId, Long partnerId) {
        this.userId = userId;
        this.partnerId = partnerId;
    }
}