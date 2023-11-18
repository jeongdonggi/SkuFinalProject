package com.SecurityBoardEx.BoardEx.chat.dto;

import com.SecurityBoardEx.BoardEx.chat.entity.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private String sender;
    private String message;
    private Long chatRoom;
    private Long chatRoomUser;
    private MessageType type;
    private LocalDateTime localDateTime;
}
