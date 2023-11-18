package com.SecurityBoardEx.BoardEx.chat.repository;

import com.SecurityBoardEx.BoardEx.chat.entity.ChatMessageEntity;
import com.SecurityBoardEx.BoardEx.chat.entity.ChatRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findAllByChatRoom(ChatRoomEntity chatRoom);
}
