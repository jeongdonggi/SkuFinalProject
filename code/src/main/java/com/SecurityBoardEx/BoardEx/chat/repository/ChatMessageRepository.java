package com.SecurityBoardEx.BoardEx.chat.repository;

import com.SecurityBoardEx.BoardEx.chat.entity.ChatMessageEntity;
import com.SecurityBoardEx.BoardEx.chat.entity.ChatRoomEntity;
import com.SecurityBoardEx.BoardEx.chat.entity.ChatRoomUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findAllByChatRoom(ChatRoomEntity chatRoom);

    // 특정 채팅방과 연결된 특정 ChatRoomUserEntity의 메시지 조회
    List<ChatMessageEntity> findAllByChatRoomAndChatRoomUser(ChatRoomEntity chatRoom, ChatRoomUserEntity chatRoomUser);
}
