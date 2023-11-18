package com.SecurityBoardEx.BoardEx.chat.controller;

import com.SecurityBoardEx.BoardEx.chat.dto.ChatMessageDto;
import com.SecurityBoardEx.BoardEx.chat.dto.ChatRoomDetailsDto;
import com.SecurityBoardEx.BoardEx.chat.dto.ParticipantDto;
import com.SecurityBoardEx.BoardEx.chat.service.ChatService;
import com.SecurityBoardEx.BoardEx.login.dto.UserInfoDto;
import com.SecurityBoardEx.BoardEx.login.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatRoomController {
    private final ChatService chatService;
    private final UserService userService;

    private final SimpMessageSendingOperations messagingTemplate;

    @GetMapping("/rooms")
    public String rooms(Model model){

        List<ChatRoomDetailsDto> allRooms = chatService.findAllRooms();

        model.addAttribute("rooms", allRooms);
        return "chat/rooms";
    }

    @GetMapping("/privaterooms")
    public String privateRooms(Model model){
        log.info("privateRoom 입장");
        Long userId = userService.findByUsername();

        List<ChatRoomDetailsDto> allRooms = chatService.findAllRooms();

        List<ChatRoomDetailsDto> privateRoom = chatService.findPrivateRoom(allRooms, userId);

        model.addAttribute("privateRoom", privateRoom);
        return "chat/privaterooms";
    }

    @GetMapping("/room/{roomId}")
    public String chatPage(@PathVariable Long roomId, Model model) throws Exception {
        log.info("chatPage에 들어옴");
        ChatRoomDetailsDto room = chatService.findByRoomId(roomId);
        boolean isprivate = room.isPrivate();

        boolean chatOut = true; // 방에서 나가졌는지

        UserInfoDto myInfo = userService.getMyInfo();
        String username = myInfo.getUsername();

        Long userId = chatService.findUserId(username);
        String nickname = chatService.findUserName(username);

        List<ChatMessageDto> messages = chatService.findMessagesByRoomId(roomId);

        if(isprivate){
            List<ParticipantDto> userInRoom = chatService.getUserInRoom(roomId);
            for(ParticipantDto user : userInRoom){
                if(user.getNickname().equals(nickname)){ // 채팅방에 들어가 있는 사람이라면
                    log.info("userInRoom? getUsername : {} username : {}", user.getNickname(), nickname);
                    chatOut = false;
                }
            }
            log.info("방에 들어있나요? : {}", chatOut);
            if(chatOut){
                return "redirect:/chat/privaterooms";
            }
        }

        model.addAttribute("roomId", roomId);
        model.addAttribute("username", nickname);
        model.addAttribute("userId", userId);
        model.addAttribute("messages", messages);
        model.addAttribute("isprivate", isprivate);

        return "chat/chat";
    }

    @GetMapping("/room/{roomId}/users")
    public String getUserList(@PathVariable Long roomId, Model model) throws Exception {
        List<ParticipantDto> userList = chatService.getUserInRoom(roomId);
        UserInfoDto myInfo = userService.getMyInfo();
        Long myId = myInfo.getId();
        model.addAttribute("myId", myId);
        model.addAttribute("userList", userList);
        model.addAttribute("roomId", roomId);
        return "chat/list";
    }

    @PostMapping("/room")
    public String createRoom(@RequestParam String roomName) throws Exception {
        UserInfoDto creator = userService.getMyInfo();
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
    public String privateRoom(Model model){
        List<UserInfoDto> allUser = userService.findAll();
        Long userId = userService.findByUsername();

        model.addAttribute("creatorId", userId);
        model.addAttribute("allUser", allUser);
        return "chat/privateroom";
    }

    @PostMapping("/privateroom")
    public String createPrivateRoom(@RequestParam String roomName,
                                    @RequestParam(required = false) List<Long> selectUsers) throws Exception {
        UserInfoDto creator = userService.getMyInfo();
        String username = creator.getUsername();
        Long userId = userService.findByUsername();
        if(selectUsers == null){
            selectUsers = new ArrayList<>();
        }
        selectUsers.add(userId);

        chatService.createRoomWithParticipants(roomName, username, selectUsers);
        return "redirect:/chat/privaterooms";
    }

    @PostMapping("/exitUser")
    public ResponseEntity<String> exitUser(@RequestParam("userId") Long userId, @RequestParam("roomId") Long roomId) throws Exception {
        UserInfoDto myInfo = userService.getMyInfo();
        String role = myInfo.getRole();

        ChatRoomDetailsDto room = chatService.findByRoomId(roomId);
        String creator = room.getCreator();

        UserInfoDto LeaveUserInfo = userService.getInfo(userId);
        String username = LeaveUserInfo.getUsername();
        log.info("username : {}", username);

        if (role == "ADMIN" || role == "MANAGER" || myInfo.getUsername().equals(creator)){
            try {
                // ChatService를 사용하여 사용자를 방출하는 비즈니스 로직을 수행합니다.
                chatService.leaveRoom(roomId, userId);

                messagingTemplate.convertAndSendToUser(username, "/queue/exit","방출되었습니다.");
                // 방출이 성공했을 때 200 OK 응답을 반환합니다.
                return ResponseEntity.ok("사용자를 방출했습니다.");
            } catch (Exception e) {
                // 방출이 실패한 경우 500 Internal Server Error 응답을 반환합니다.
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("사용자 방출에 실패했습니다.");
            }
        }else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("사용자 권한이 없습니다.");
        }
    }
}
