package com.SecurityBoardEx.BoardEx.board.service;

import com.SecurityBoardEx.BoardEx.board.condition.BoardSearchCondition;
import com.SecurityBoardEx.BoardEx.board.dto.BoardInfoDto;
import com.SecurityBoardEx.BoardEx.board.dto.BoardPagingDto;
import com.SecurityBoardEx.BoardEx.board.dto.BoardSaveDto;
import com.SecurityBoardEx.BoardEx.board.dto.BoardUpdateDto;
import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.SecurityBoardEx.BoardEx.board.exception.BoardException;
import com.SecurityBoardEx.BoardEx.board.exception.BoardExceptionType;
import com.SecurityBoardEx.BoardEx.board.repository.BoardRepository;
import com.SecurityBoardEx.BoardEx.config.SecurityUtil;
import com.SecurityBoardEx.BoardEx.file.entity.FileEntity;
import com.SecurityBoardEx.BoardEx.file.exception.FileException;
import com.SecurityBoardEx.BoardEx.file.service.FileService;
import com.SecurityBoardEx.BoardEx.login.exception.UserException;
import com.SecurityBoardEx.BoardEx.login.exception.UserExceptionType;
import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final FileService fileService;

    /** 게시글 등록 **/
    public Long save(BoardSaveDto boardSaveDto) throws FileException {
        BoardEntity board = boardSaveDto.toEntity(); //Entity로 변경
        // 쓰인 날짜는 자동 생성
        // 여기서 writer 설정
        board.confirmWriter(userRepository.findByUsername(SecurityUtil.getLoginUsername())
                .orElseThrow(() -> new UserException(UserExceptionType.NOT_FOUND_USER)));

        System.out.println("boardSaveDto.uploadFile() = " + boardSaveDto.uploadFile());
        // 여기서 파일 설정
        List<MultipartFile> files = boardSaveDto.uploadFile();
        if(files != null && !files.isEmpty()){ // 만약 파일이 있다면
            fileService.boardSaveFile(files, board);
        }

        BoardEntity boardEntity = boardRepository.save(board);
        return boardEntity.getId();
    }

    /** 게시글 수정 **/
    public void updated(Long id, BoardUpdateDto boardUpdateDto){
        BoardEntity board = boardRepository.findById(id).orElseThrow(() ->
                new BoardException(BoardExceptionType.BOARD_NOT_FOUND));

        checkAuthority(board, BoardExceptionType.NOT_AUTHORITY_UPDATE_BOARD);

        boardUpdateDto.title().ifPresent(board::updateTitle);
        boardUpdateDto.content().ifPresent(board::updateContent);

        List<MultipartFile> newFiles = boardUpdateDto.uploadFile() != null ? boardUpdateDto.uploadFile() : new ArrayList<>();
        fileService.boardSaveFile(newFiles, board);

        boardRepository.save(board);
    }

    /** 게시글 삭제 **/
    public void delete(Long id){
        BoardEntity board = boardRepository.findById(id).orElseThrow(() ->
                new BoardException(BoardExceptionType.BOARD_NOT_FOUND));

        checkAuthority(board, BoardExceptionType.NOT_AUTHORITY_DELETE_BOARD);

        // 연결된 파일들 삭제
        board.getFileEntityList().forEach(fileEntity -> {
            fileService.delete(fileEntity.getFilePath());
        });

        boardRepository.delete(board);
    }

    private void checkAuthority(BoardEntity board, BoardExceptionType boardExceptionType){
        if(!board.getWriter().getUsername().equals(SecurityUtil.getLoginUsername()))
            throw new BoardException(boardExceptionType);
    }

    /** 게시글 1개 조회 **/
    public BoardInfoDto getBoardInfo(Long id){
        /* board + user 조회 -> 쿼리 1번 발생
           댓글&대댓글 리스트 조회 -> 쿼리 1번 발생(board id로 찾는 것이므로, where문 발생)
           댓글 작성자 정보 조회 -> 배치 사이즈를 이용하였기 떄문에 쿼리 1번 혹은 N/배치 사이즈 만큼 발생
        */
        return new BoardInfoDto(boardRepository.findWithWriterById(id)
                .orElseThrow(()-> new BoardException(BoardExceptionType.BOARD_NOT_FOUND)));
    }

    /** 검색 조건에 따른 게시글 페이징 **/
    public BoardPagingDto getBoardList(Pageable pageable){
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 2; // 한 페이지에 보여줄 개수
        // return new BoardPagingDto(boardRepository.findAll(pageable));
        return new BoardPagingDto(boardRepository.findAll(PageRequest.of(page,pageLimit, Sort.by(Sort.Direction.DESC, "id"))));
    }

    /** 검색 조건에 따른 게시글 리스트 조회 + 페이징 **/
    public BoardPagingDto getBoardList(Pageable pageable, BoardSearchCondition boardSearchCondition){
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 2; // 한 페이지에 보여줄 개수
        return new BoardPagingDto(boardRepository.search(boardSearchCondition, PageRequest.of(page, pageLimit, Sort.Direction.DESC, "id")));
    }
}
