package com.SecurityBoardEx.BoardEx.chat.entity;

import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_table")
@Getter @Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoomEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @Column(nullable = false)
    private String roomName;

    @Column
    private int count; // 사람 수

    @Column
    private boolean isPrivate; //공개 or 개인

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private UserEntity creator;

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessageEntity> messages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomUserEntity> participants = new ArrayList<>();

    public void addParticipant(ChatRoomUserEntity participant) {
        this.participants.add(participant);
        if (participant.getChatRoom() != this) {
            participant.setChatRoom(this);
        }
    }
}

