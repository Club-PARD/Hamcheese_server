package pard.server.com.longkathon.chat;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_sendbird_mapping",
        uniqueConstraints = @UniqueConstraint(name = "uk_sendbird_user", columnNames = {"sendbirdUserId"}))
public class UserSendbirdMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long localUserId;

    @Column(nullable = false, length = 100)
    private String sendbirdUserId;
}
