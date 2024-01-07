package com.SecurityBoardEx.BoardEx.board.repository;

import com.SecurityBoardEx.BoardEx.board.condition.BoardSearchCondition;
import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.SecurityBoardEx.BoardEx.board.entity.QBoardEntity.*;
import static com.SecurityBoardEx.BoardEx.login.entity.QUserEntity.*;

public class CustomBoardRepositoryImpl implements CustomBoardRepository{

    private final JPAQueryFactory query;

    @Autowired
    public CustomBoardRepositoryImpl(EntityManager em) { // repository 겹쳐서 오류 생겼음
        this.query = new JPAQueryFactory(em);
    }

    @Override
    public Page<BoardEntity> search(BoardSearchCondition boardSearchCondition, Pageable pageable){
        BooleanBuilder builder = new BooleanBuilder();

        if(StringUtils.hasLength(boardSearchCondition.getTitle())){
            builder.or(boardEntity.title.contains(boardSearchCondition.getTitle()));
        }
        if(StringUtils.hasLength(boardSearchCondition.getContent())){
            builder.or(boardEntity.content.contains(boardSearchCondition.getContent()));
        }

        List<BoardEntity> content = query.selectFrom(boardEntity)
                .where(
//                        contentHasStr(boardSearchCondition.getContent()),
//                        titleHasStr(boardSearchCondition.getTitle())
                        builder
                )
                .leftJoin(boardEntity.writer, userEntity) // board.writer에 userEntity값이 연결됨
                .fetchJoin() // 한번에 가져오기
                .orderBy(boardEntity.createdDate.desc()) // 페이징
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<BoardEntity> countQuery = query.selectFrom(boardEntity) // 총 게시글 찾는 쿼리
                .where(
                        contentHasStr(boardSearchCondition.getContent()),
                        titleHasStr(boardSearchCondition.getTitle())
                );
        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());
    }

    private BooleanExpression contentHasStr(String content){
        return StringUtils.hasLength(content) ? boardEntity.content.contains(content) : null;
    }

    private BooleanExpression titleHasStr(String title){
        return StringUtils.hasLength(title) ? boardEntity.title.contains(title) : null;
    }
}
