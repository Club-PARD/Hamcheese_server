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
        name = "webhook_inbox",
        uniqueConstraints = @UniqueConstraint(name = "uk_webhook_dedup", columnNames = {"dedupKey"})
)
public class WebhookInbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;

    @Column(nullable = false, length = 300)
    private String dedupKey; // 멱등성 키(중복 webhook 방지)

    @Column(length = 200)
    private String signature;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String payloadRaw;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(length = 2000)
    private String errorMessage;

    @CreationTimestamp
    private LocalDateTime receivedAt;

    public enum Status {
        RECEIVED, PROCESSED, FAILED
    }

    public void markProcessed() {
        this.status = Status.PROCESSED;
        this.errorMessage = null;
    }

    public void markFailed(String msg) {
        this.status = Status.FAILED;
        this.errorMessage = msg;
    }
}
