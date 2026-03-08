package pard.server.com.longkathon.config.webSocket.chatRoom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pard.server.com.longkathon.config.webSocket.dto.ChatRoomWithLastMessageDto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT c FROM ChatRoom c WHERE (c.userId = :userId AND c.partnerId = :partnerId) OR (c.userId = :partnerId AND c.partnerId = :userId)")
    Optional<ChatRoom> findChatRoomByUsers(@Param("userId") Long userId, @Param("partnerId") Long partnerId);

    // 마지막 메시지 포함하여 내 채팅방 조회 (N+1 방지)
    @Query("""
        SELECT new pard.server.com.longkathon.config.webSocket.dto.ChatRoomWithLastMessageDto(
            cr.id,
            cr.userId,
            cr.partnerId,
            (SELECT cm.content FROM ChatMessage cm
             WHERE cm.chatRoomId = cr.id
             ORDER BY cm.createdAt DESC LIMIT 1),
            (SELECT cm.createdAt FROM ChatMessage cm
             WHERE cm.chatRoomId = cr.id
             ORDER BY cm.createdAt DESC LIMIT 1)
        )
        FROM ChatRoom cr
        WHERE cr.userId = :userId OR cr.partnerId = :userId
        ORDER BY (SELECT cm.createdAt FROM ChatMessage cm
                  WHERE cm.chatRoomId = cr.id
                  ORDER BY cm.createdAt DESC LIMIT 1) DESC NULLS LAST
    """)
    List<ChatRoomWithLastMessageDto> findMyRoomsWithLastMessage(@Param("userId") Long userId);
}
