package com.SecurityBoardEx.BoardEx.board.repository;

import com.SecurityBoardEx.BoardEx.board.condition.BoardSearchCondition;
import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
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
        List<BoardEntity> content = query.selectFrom(boardEntity)
                .where(
                        contentHasStr(boardSearchCondition.getContent()),
                        titleHasStr(boardSearchCondition.getTitle())
                )
                .leftJoin(boardEntity.writer, userEntity)
                .fetchJoin()
                .orderBy(boardEntity.createdDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<BoardEntity> countQuery = query.selectFrom(boardEntity)
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
