package pard.server.com.longkathon.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findByChannelUrlAndMessageId(String channelUrl, Long messageId);
}
