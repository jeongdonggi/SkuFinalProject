package com.SecurityBoardEx.BoardEx.login.entity;

import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.chat.entity.ChatRoomEntity;
import com.SecurityBoardEx.BoardEx.chat.entity.ChatRoomUserEntity;
import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Table(name = "user_table")
public class UserEntity extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id; // primary key

    @Column(nullable = false, unique = true)
    private String username; // 이메일로 로그인
    private String password; // 비밀번호
    private String nickname; // 실명

    private String refreshToken; // 리프레시 토큰

    @Enumerated(EnumType.STRING)
    private Role role; // 현재 GUEST ,USER, MANGER, ADMIN

    // 유저 권한 설정 메소드
    public void authorizeUser(Role role){
        this.role = role;
    }

    //== 회원탈퇴 -> 작성한 게시물, 댓글 모두 삭제 ==//
    @Builder.Default
    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BoardEntity> boardList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> commentList = new ArrayList<>();

    /** 채팅 **/
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomUserEntity> chatRooms = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomEntity> createdRooms = new ArrayList<>();

    //== 연관관계 메서드 ==//
    public void addBoard(BoardEntity board){
        //post의 writer 설정은 post에서 함
        boardList.add(board);
    }

    public void addComment(CommentEntity comment){
        //comment의 writer 설정은 comment에서 함
        commentList.add(comment);
    }

    /** 채팅 **/
    public void addCreatedRoom(ChatRoomEntity room) {
        this.createdRooms.add(room);
        if (room.getCreator() != this) {
            room.setCreator(this);
        }
    }
    public void addChatRoomUser(ChatRoomUserEntity chatRoomUser) {
        this.chatRooms.add(chatRoomUser);
        if (chatRoomUser.getUser() != this) {
            chatRoomUser.setUser(this);
        }
    }

    // 정보 수정
    public void updatePassword(PasswordEncoder passwordEncoder, String password){
        this.password = passwordEncoder.encode(password);
    }
    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

    //비밀번호 변경, 회원 탈퇴 시 비밀번호의 일치여부를 판단하는 메서드.
    public boolean matchPassword(PasswordEncoder passwordEncoder, String checkPassword){
        return passwordEncoder.matches(checkPassword, getPassword());
    }

    //회원가입시, USER의 권한을 부여하는 메서드.
    public void addUserAuthority() {
        this.role = Role.GUEST;
    }

    // 패스워드 암호화
    public void passwordEncode(PasswordEncoder passwordEncoder){
        this.password = passwordEncoder.encode(this.password);
    }

    // 리프레시 토큰 업데이트
    public void updateRefreshToken(String updateRefreshToken){
        this.refreshToken = updateRefreshToken;
    }
}

