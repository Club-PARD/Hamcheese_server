package pard.server.com.longkathon.chat;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookInboxRepo extends JpaRepository<WebhookInbox, Long> {
    boolean existsByDedupKey(String dedupKey);
}
