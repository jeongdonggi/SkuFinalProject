package com.SecurityBoardEx.BoardEx.chat.entity;

import com.SecurityBoardEx.BoardEx.login.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "message_table")
public class ChatMessageEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private ChatRoomEntity chatRoom;

    @ManyToOne
    @JoinColumn(name = "roomuser_id")
    private ChatRoomUserEntity chatRoomUser;

    @Column
    private String nickname;

    @Column
    private String message;

    @Column
    private MessageType type;
}
