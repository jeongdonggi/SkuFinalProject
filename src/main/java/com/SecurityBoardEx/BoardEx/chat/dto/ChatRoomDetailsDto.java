package com.SecurityBoardEx.BoardEx.chat.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDetailsDto {
    private Long id;
    private String roomName;
    private String creator;
    private int count;
    private boolean isPrivate;
    private List<ParticipantDto> participants;
}
