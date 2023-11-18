package com.SecurityBoardEx.BoardEx.chat.entity;

import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "roomuser_table")
public class ChatRoomUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private ChatRoomEntity chatRoom;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public void setUser(UserEntity user) {
        this.user = user;
        if (!user.getChatRooms().contains(this)) {
            user.addChatRoomUser(this);
        }
    }

    public void setChatRoom(ChatRoomEntity chatRoom) {
        this.chatRoom = chatRoom;
        if (!chatRoom.getParticipants().contains(this)) {
            chatRoom.addParticipant(this);
        }
    }
}
