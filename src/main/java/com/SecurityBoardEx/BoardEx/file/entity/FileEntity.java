package com.SecurityBoardEx.BoardEx.file.entity;

import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.login.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "file_table")
public class FileEntity extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long id;

    @Column
    private String filePath;

    @Column
    private String fileName;

    @Column
    private boolean isImage;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private BoardEntity board;

    @Builder
    public FileEntity(BoardEntity board, String filePath, String fileName, boolean isImage){
        this.board = board;
        this.filePath = filePath;
        this.fileName = fileName;
        this.isImage = isImage;
    }

    // 연관관계 편의 메서드
    public void setBoard(BoardEntity board) {
        this.board = board;
    }
}
