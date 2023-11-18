package com.SecurityBoardEx.BoardEx.chat.controller;

import com.SecurityBoardEx.BoardEx.chat.dto.ChatMessageDto;
import com.SecurityBoardEx.BoardEx.chat.dto.ChatRoomDetailsDto;
import com.SecurityBoardEx.BoardEx.chat.dto.ParticipantDto;
import com.SecurityBoardEx.BoardEx.chat.service.ChatService;
import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {
    private final ChatService chatService;
    private final UserService userService;

    @GetMapping("/rooms")
    public String rooms(Model model, @AuthenticationPrincipal UserDetails user){
        log.info("rooms에 입장");
        String username = user.getUsername();
        Long userId = chatService.findUserId(username);

        List<ChatRoomDetailsDto> allRooms = chatService.findAllRooms();

        List<ChatRoomDetailsDto> privateRoom = chatService.findPrivateRoom(allRooms, userId);


        model.addAttribute("rooms", allRooms);
        model.addAttribute("privateRoom", privateRoom);
        return "chat/rooms";
    }

    @GetMapping("/room/{roomId}")
    public String chatPage(@PathVariable Long roomId, @AuthenticationPrincipal UserDetails user, Model model){
        log.info("chatPage에 들어옴");
        ChatRoomDetailsDto room = chatService.findByRoomId(roomId);
        String username = user.getUsername();

        Long userId = chatService.findUserId(username);
        String nickname = chatService.findUserName(username);

        List<ChatMessageDto> messages = chatService.findMessagesByRoomId(roomId);

        model.addAttribute("roomId", roomId);
        model.addAttribute("username", nickname);
        model.addAttribute("userId", userId);
        model.addAttribute("messages", messages);

        return "chat/chat";
    }

    @GetMapping("/room/{roomId}/users")
    public String getUserList(@PathVariable Long roomId, Model model){
        List<ParticipantDto> userList = chatService.getUserInRoom(roomId);
        model.addAttribute("userList", userList);
        return "chat/list";
    }

    @PostMapping("/room")
    public String createRoom(@RequestParam String roomName, @AuthenticationPrincipal UserDetails creator){
        String username = creator.getUsername();
        chatService.createRoom(roomName, username);
        return "redirect:/chat/rooms";
    }

    @GetMapping("/userCount/{roomId}")
    public ResponseEntity<Integer> getUserCount(@PathVariable Long roomId) {
        log.info("userCount 들어옴");
        int userCount = chatService.userCount(roomId); // ChatService를 통해 사용자 수를 가져옵니다.
        log.info("userCount : {}",String.valueOf(userCount));
        return ResponseEntity.ok(userCount);
    }

    @GetMapping("/private")
    public String privateRoom(Model model, @AuthenticationPrincipal UserDetails creator){
        List<UserInfoDto> allUser = userService.findAll();
        String username = creator.getUsername();
        Long userId = userService.findByUsername(username);

        model.addAttribute("creatorId", userId);
        model.addAttribute("allUser", allUser);
        return "chat/privateroom";
    }

    @PostMapping("/privateroom")
    public String createPrivateRoom(@RequestParam String roomName,
                                    @RequestParam List<Long> selectUsers,
                                    @AuthenticationPrincipal UserDetails creator){
        String username = creator.getUsername();
        Long userId = userService.findByUsername(username);
        selectUsers.add(userId);

        chatService.createRoomWithParticipants(roomName, username, selectUsers);
        return "redirect:/chat/rooms";
    }
}
