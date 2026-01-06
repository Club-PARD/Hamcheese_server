package pard.server.com.longkathon.chat;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "chat_message",
        uniqueConstraints = @UniqueConstraint(name = "uk_channel_message", columnNames = {"channelUrl", "messageId"})
)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String channelUrl;

    @Column(nullable = false)
    private Long messageId; // sendbird message_id

    @Column(length = 50)
    private String messageType; // USER / ADMIN / FILE 등

    @Column(length = 100)
    private String senderSendbirdUserId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String message; // 텍스트 메시지

    @Column(length = 100)
    private String customType;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String data; // sendbird message.data (optional)

    private Long sentAt; // sendbird timestamp(ms) - 있으면 저장

    private boolean deleted;

    private Long updatedAt; // sendbird timestamp(ms)

    @CreationTimestamp
    private LocalDateTime createdAt;

    public void updateFromWebhook(String message, String customType, String data, Long updatedAt) {
        this.message = message;
        this.customType = customType;
        this.data = data;
        this.updatedAt = updatedAt;
    }

    public void markDeleted(Long updatedAt) {
        this.deleted = true;
        this.updatedAt = updatedAt;
    }
}
