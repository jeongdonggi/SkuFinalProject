package com.SecurityBoardEx.BoardEx.board.entity;

import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
import com.SecurityBoardEx.BoardEx.file.entity.FileEntity;
import com.SecurityBoardEx.BoardEx.login.entity.BaseTimeEntity;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "board_table")
public class BoardEntity extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;

    @Column
    private boolean restrictedAccess;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private UserEntity writer;

    @Column(length = 40, nullable = false)
    private String title;

    @Lob // 255 글자를 넘어갈 때 사용
    @Column(nullable = false)
    private String content;

    @Builder
    public BoardEntity(String title, String content, boolean restrictedAccess){
        this.title = title;
        this.content = content;
        this.restrictedAccess = restrictedAccess;
    }

    /** 게시글을 삭제하면 달려있는 댓글, 파일 모두 삭제 **/
    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CommentEntity> commentEntityList = new ArrayList<>();

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FileEntity> fileEntityList = new ArrayList<>();

    /** 연관관계 편의 메서드 **/
    public void confirmWriter(UserEntity writer){
        this.writer = writer;
        writer.addBoard(this);
    }

    public void addComment(CommentEntity comment) {
        commentEntityList.add(comment);
    }

    public void addFile(FileEntity file) {
        fileEntityList.add(file);
        file.setBoard(this);
    }

    public boolean isRestrictedAccess() {
        return restrictedAccess;
    }

    /** 내용 수정 **/
    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void setRestrictedAccess(boolean restrictedAccess){
        this.restrictedAccess = restrictedAccess;
    }
}
