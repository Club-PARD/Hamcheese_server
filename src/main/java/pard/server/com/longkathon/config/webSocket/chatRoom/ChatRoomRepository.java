package pard.server.com.longkathon.config.webSocket.chatRoom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT c FROM ChatRoom c WHERE (c.userId = :userId AND c.sellerId = :sellerId) OR (c.userId = :sellerId AND c.sellerId = :userId)")
    Optional<ChatRoom> findChatRoomByUsers(@Param("userId") Long userId, @Param("sellerId") Long sellerId);
}
