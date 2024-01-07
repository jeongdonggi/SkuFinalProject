package com.SecurityBoardEx.BoardEx.chat.repository;

import com.SecurityBoardEx.BoardEx.chat.entity.ChatRoomEntity;
import com.SecurityBoardEx.BoardEx.chat.entity.ChatRoomUserEntity;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUserEntity, Long> {
    Optional<ChatRoomUserEntity> findByChatRoomAndUser(ChatRoomEntity chatRoom, UserEntity user);
    List<ChatRoomUserEntity> findAllByChatRoom(ChatRoomEntity chatRoom);
}
