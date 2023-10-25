package com.SecurityBoardEx.BoardEx.comment.entity;

import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.login.entity.BaseTimeEntity;
import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "comment_table")
public class CommentEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id")
    private UserEntity writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private BoardEntity board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentEntity parent; // null이면 부모

    @Lob
    @Column(nullable = false)
    private String content;

    private boolean isRemoved = false;

    /** 부모 댓글을 삭제해도 자식 댓글은 남아 있음 **/
    @OneToMany(mappedBy = "parent")
    private List<CommentEntity> childList = new ArrayList<>();

    /** 연관 관계 편의 메서드 **/
    public void confirmWriter(UserEntity writer){
        this.writer = writer;
        writer.addComment(this);
    }

    public void confirmBoard(BoardEntity board){
        this.board = board;
        board.addComment(this);
    }

    public void confirmParent(CommentEntity parent){
        this.parent = parent;
        parent.addChild(this);
    }

    public void addChild(CommentEntity child){
        childList.add(child);
    }

    /** 수정 **/
    public void updateContent(String content){
        this.content = content;
    }

    /** 삭제 **/
    public void remove(){
        this.isRemoved = true;
    }

    @Builder
    public CommentEntity (UserEntity writer, BoardEntity board, CommentEntity parent, String content){
        this.writer = writer;
        this.board = board;
        this.parent = parent;
        this.content = content;
        this.isRemoved = false;
    }

    /** 비즈니스 로직 **/
    public List<CommentEntity> findRemovableList(){
        List<CommentEntity> result = new ArrayList<>();
        Optional.ofNullable(this.parent).ifPresentOrElse(
                parentComment ->{
                    // 대댓글인 경우
                    if(parentComment.isRemoved() && parentComment.isAllChildRemoved()){
                        result.addAll(parentComment.getChildList());
                        result.add(parentComment);
                    }
                },
                () ->{
                    // 댓글인 경우
                    if(isAllChildRemoved()){
                        result.add(this);
                        result.addAll(this.getChildList());
                    }
                }
        );
        return result;
    }

    // 모든 자식들이 삭제 되어 있는지 판단
    private boolean isAllChildRemoved(){
        return getChildList().stream()
                .map(CommentEntity::isRemoved)// 지워졌는지 여부
                .filter(isRemove -> !isRemove)
                .findAny() // 지워진게 남았다면 false
                .orElse(true); // 다 지워졌다면 true
    }
}
