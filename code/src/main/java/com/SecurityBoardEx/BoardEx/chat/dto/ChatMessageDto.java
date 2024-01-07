package com.SecurityBoardEx.BoardEx.chat.dto;

import com.SecurityBoardEx.BoardEx.chat.entity.MessageType;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

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
