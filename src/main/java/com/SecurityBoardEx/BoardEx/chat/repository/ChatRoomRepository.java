package com.SecurityBoardEx.BoardEx.chat.repository;

import com.SecurityBoardEx.BoardEx.chat.entity.ChatRoomEntity;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {
    Optional<ChatRoomEntity> findByRoomName(String roomName);
}
