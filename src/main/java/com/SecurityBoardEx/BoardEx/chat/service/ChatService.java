package com.SecurityBoardEx.BoardEx.chat.service;

import com.SecurityBoardEx.BoardEx.chat.dto.ChatMessageDto;
import com.SecurityBoardEx.BoardEx.chat.dto.ChatRoomDetailsDto;
import com.SecurityBoardEx.BoardEx.chat.dto.ParticipantDto;
import com.SecurityBoardEx.BoardEx.chat.entity.ChatMessageEntity;
import com.SecurityBoardEx.BoardEx.chat.entity.ChatRoomEntity;
import com.SecurityBoardEx.BoardEx.chat.entity.ChatRoomUserEntity;
import com.SecurityBoardEx.BoardEx.chat.repository.ChatMessageRepository;
import com.SecurityBoardEx.BoardEx.chat.repository.ChatRoomRepository;
import com.SecurityBoardEx.BoardEx.chat.repository.ChatRoomUserRepository;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import com.SecurityBoardEx.BoardEx.login.exception.UserException;
import com.SecurityBoardEx.BoardEx.login.exception.UserExceptionType;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public List<ChatRoomDetailsDto> findAllRooms(){
        return chatRoomRepository.findAll().stream()
                .map(this::convertToChatRoomDetailsDto)
                .collect(Collectors.toList());
    }

    public ChatRoomDetailsDto findByRoomId(Long roomId){
        ChatRoomEntity roomEntity = chatRoomRepository.findById(roomId).orElseThrow(() ->
                new IllegalArgumentException("Room Not Found"));
        return convertToChatRoomDetailsDto(roomEntity);
    }

    public void createRoom(String roomName, String username){
        UserEntity creator = userRepository.findByUsername(username).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        ChatRoomEntity newRoom = ChatRoomEntity.builder()
                .creator(creator)
                .roomName(roomName)
                .isPrivate(false)
                .build();
        chatRoomRepository.save(newRoom);
    }

    public void createRoomWithParticipants(String roomName, String creatorUsername, List<Long> participantIds){
        UserEntity creator = userRepository.findByUsername(creatorUsername).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        // 채팅방 생성
        ChatRoomEntity newRoom = ChatRoomEntity.builder()
                .creator(creator)
                .roomName(roomName)
                .isPrivate(true)
                .build();
        ChatRoomEntity saveRoom = chatRoomRepository.save(newRoom);

        // 초대할 사용자들을 참가자로 추가
        for(Long userId : participantIds){
            UserEntity participant = userRepository.findById(userId).orElseThrow(() ->
                    new UserException(UserExceptionType.NOT_FOUND_USER));

            ChatRoomUserEntity newUser = new ChatRoomUserEntity();
            newUser.setUser(participant);
            newUser.setChatRoom(saveRoom);

            chatRoomUserRepository.save(newUser);
        }
        saveRoom.setCount(participantIds.size());
        chatRoomRepository.save(saveRoom);
    }

    public boolean joinRoom(Long roomId, Long userId){
        log.info("joinRoom 들어왔다.");
        ChatRoomEntity room = chatRoomRepository.findById(roomId).orElseThrow(() ->
            new IllegalArgumentException("Room not found"));

        UserEntity user = userRepository.findById(userId).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        Optional<ChatRoomUserEntity> existingChatRoomUser = chatRoomUserRepository.findByChatRoomAndUser(room, user);
        if (existingChatRoomUser.isPresent()) {
            return false;
        }

        ChatRoomUserEntity newUser = new ChatRoomUserEntity();
        newUser.setUser(user);
        newUser.setChatRoom(room);

        chatRoomUserRepository.save(newUser);

        room.setCount(room.getParticipants().size());
        chatRoomRepository.save(room);

        return true;
    }

    public ChatMessageDto saveChatMessage(ChatMessageDto chatMessage){
        log.info("saveChatMessage 도착");
        ChatMessageEntity chatMessageEntity = convertToChatMessageEntity(chatMessage);
        chatMessageEntity.setType(chatMessage.getType());
        log.info("변환된 Entity의 메시지: {}", chatMessageEntity.getMessage()); // Entity 변환 후 메시지 확인
        log.info("변환된 Entity의 타입: {}", chatMessageEntity.getType());
        ChatMessageEntity save = chatMessageRepository.save(chatMessageEntity);
        return convertToChatMessageDto(save);
    }

    public List<ChatMessageDto> findMessagesByRoomId(Long roomId){
        ChatRoomEntity room = chatRoomRepository.findById(roomId).orElseThrow(()->
                new IllegalArgumentException("Room not found"));

        List<ChatMessageEntity> messages = chatMessageRepository.findAllByChatRoom(room);

        return messages.stream()
                .map(this::convertToChatMessageDto)
                .collect(Collectors.toList());
    }

    public void leaveRoom(Long roomId, Long userId){
        log.info("leaveRoom 들어왔다.");
        ChatRoomEntity room = chatRoomRepository.findById(roomId).orElseThrow(() ->
                new IllegalArgumentException("Room not found"));

        UserEntity user = userRepository.findById(userId).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));

        ChatRoomUserEntity chatRoomUser = chatRoomUserRepository.findByChatRoomAndUser(room, user)
                .orElseThrow(() -> new IllegalArgumentException("User not part of the room"));

        room.getParticipants().remove(chatRoomUser);
        chatRoomUserRepository.delete(chatRoomUser);
        room.setCount(room.getParticipants().size());
        chatRoomRepository.save(room);
    }

    public int userCount(Long roomId){
        ChatRoomEntity room = chatRoomRepository.findById(roomId).orElseThrow(() ->
                new IllegalArgumentException("Room not found"));
        return room.getCount();
    }

    // 채팅방에 있는 사용자의 이름 목록
    public List<ParticipantDto> getUserInRoom(Long roomId){
        ChatRoomEntity room = chatRoomRepository.findById(roomId).orElseThrow(() ->
                new IllegalArgumentException("Room not found"));

        return chatRoomUserRepository.findAllByChatRoom(room).stream()
                .map(chatRoomUser -> new ParticipantDto(chatRoomUser.getUser().getId(), chatRoomUser.getUser().getNickname()))
                .collect(Collectors.toList());
    }

    /** 유저 찾기 **/
    public Long findUserId(String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));
        return user.getId();
    }

    public String findUserName(String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() ->
                new UserException(UserExceptionType.NOT_FOUND_USER));
        return user.getNickname();
    }

    public List<ChatRoomDetailsDto> findPrivateRoom(List<ChatRoomDetailsDto> allRoom, Long userId){
        List<ChatRoomDetailsDto> PrivateRoom = new ArrayList<>();

        for(ChatRoomDetailsDto room : allRoom){
            if(room.isPrivate()){
                for(ParticipantDto participant : room.getParticipants()){
                    if(participant.getUserId() == userId){
                        PrivateRoom.add(room);
                    }
                }
            }
        }
        return PrivateRoom;
    }

    /** 엔티티 변경 **/
    private ChatRoomDetailsDto convertToChatRoomDetailsDto(ChatRoomEntity roomEntity) {
        List<ParticipantDto> participantDtos = roomEntity.getParticipants().stream()
                .map(participant -> new ParticipantDto(
                        participant.getUser().getId(),
                        participant.getUser().getUsername()))
                .collect(Collectors.toList());

        return new ChatRoomDetailsDto(
                roomEntity.getId(),
                roomEntity.getRoomName(),
                roomEntity.getCreator().getUsername(),
                roomEntity.getCount(),
                roomEntity.isPrivate(),
                participantDtos
        );
    }

    private ChatMessageEntity convertToChatMessageEntity(ChatMessageDto chatMessageDto) {
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();

        ChatRoomEntity chatRoom = chatRoomRepository.findById(chatMessageDto.getChatRoom())
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found with id: " + chatMessageDto.getChatRoom()));
        chatMessageEntity.setChatRoom(chatRoom);

        ChatRoomUserEntity chatRoomUser = chatRoomUserRepository.findById(chatMessageDto.getChatRoomUser())
                .orElseThrow(() -> new IllegalArgumentException("Chat room user not found with id: " + chatMessageDto.getChatRoomUser()));
        chatMessageEntity.setChatRoomUser(chatRoomUser);

        chatMessageEntity.setMessage(chatMessageDto.getMessage());
        chatMessageEntity.setType(chatMessageDto.getType());

        // BaseTimeEntity가 자동으로 생성 및 업데이트 날짜를 관리한다고 가정할 때, localDateTime은 여기서 설정하지 않습니다.

        return chatMessageEntity;
    }

    private ChatMessageDto convertToChatMessageDto(ChatMessageEntity chatMessageEntity) {
        ChatMessageDto chatMessageDto = new ChatMessageDto();
        chatMessageDto.setId(chatMessageEntity.getId());
        chatMessageDto.setSender(chatMessageEntity.getChatRoomUser().getUser().getNickname());
        chatMessageDto.setMessage(chatMessageEntity.getMessage());
        chatMessageDto.setChatRoom(chatMessageEntity.getChatRoom().getId());
        chatMessageDto.setChatRoomUser(chatMessageEntity.getChatRoomUser().getId());
        chatMessageDto.setType(chatMessageEntity.getType());
        chatMessageDto.setLocalDateTime(chatMessageEntity.getCreatedDate()); // BaseTimeEntity의 CreatedDate를 사용합니다.

        return chatMessageDto;
    }
}
