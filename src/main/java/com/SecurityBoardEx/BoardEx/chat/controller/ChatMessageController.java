package com.SecurityBoardEx.BoardEx.chat.controller;

import com.SecurityBoardEx.BoardEx.chat.dto.ChatMessageDto;
import com.SecurityBoardEx.BoardEx.chat.entity.ChatMessageEntity;
import com.SecurityBoardEx.BoardEx.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/join")
    private void joinRoom(@Payload ChatMessageDto chatMessage){
        Long roomId = chatMessage.getChatRoom();
        Long userId=  chatMessage.getChatRoomUser();

        boolean join = chatService.joinRoom(roomId, userId);

        log.info("join : {}", join);

        if(join){
            String nickname = chatMessage.getSender();
            log.info(nickname);

            chatMessage.setMessage(nickname + "님이 입장하였습니다.");
            chatService.saveChatMessage(chatMessage);
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, chatMessage);
        }
    }

    @MessageMapping("/chat/send")
    public void sendMessage(@Payload ChatMessageDto chatMessage){
        Long roomId = chatMessage.getChatRoom();
        log.info("chat.send : {}",roomId);
        log.info("메시지: {}", chatMessage.getMessage());
        log.info("타입: {}", chatMessage.getType());
        chatService.saveChatMessage(chatMessage);
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, chatMessage);
    }

    @MessageMapping("/chat/leave")
    public void leaveRoom(@Payload ChatMessageDto chatMessage){
        Long roomId = chatMessage.getChatRoom();
        Long userId = chatMessage.getChatRoomUser();

        String nickname = chatMessage.getSender();

        chatMessage.setMessage(nickname + "님이 퇴장하였습니다.");
        chatService.saveChatMessage(chatMessage);
        chatService.leaveRoom(roomId, userId);
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, chatMessage);
    }
}
