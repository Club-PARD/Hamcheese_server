package pard.server.com.longkathon.config.webSocket.chatMessage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pard.server.com.longkathon.config.webSocket.chatRoom.ChatRoom;
import pard.server.com.longkathon.config.webSocket.dto.ChatMessageResponse;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("""
    select new pard.server.com.longkathon.config.webSocket.dto.ChatMessageResponse(
        m.id,
        m.chatRoomId,
        m.senderId,
        m.content,
        u.name,
        m.createdAt
    )
    from ChatMessage m
    join User u on u.userId = m.senderId
    where m.chatRoomId = :chatRoomId
    order by m.createdAt asc
""")
    List<ChatMessageResponse> findMessagesWithUserByChatRoomId(@Param("chatRoomId") Long chatRoomId);

}
