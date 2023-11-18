//package com.SecurityBoardEx.BoardEx;
//
//import com.SecurityBoardEx.BoardEx.board.entity.BoardEntity;
//import com.SecurityBoardEx.BoardEx.board.repository.BoardRepository;
//import com.SecurityBoardEx.BoardEx.comment.entity.CommentEntity;
//import com.SecurityBoardEx.BoardEx.comment.repository.CommentRepository;
//import com.SecurityBoardEx.BoardEx.login.entity.Role;
//import com.SecurityBoardEx.BoardEx.login.entity.UserEntity;
//import com.SecurityBoardEx.BoardEx.login.repositroy.UserRepository;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.crypto.factory.PasswordEncoderFactories;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import static java.lang.Long.parseLong;
//import static java.lang.String.format;
//import static java.lang.String.valueOf;
//
//@RequiredArgsConstructor
//@Component
//public class InitService {
//    private final Init init;
//
//    @PostConstruct // 해당 빈 자체만 생성되었다고 가정하고 호출
//    public void init(){
//        init.save();
//    }
//
//    @RequiredArgsConstructor
//    @Component
//    private static class Init{
//        private final UserRepository userRepository;
//        private final BoardRepository boardRepository;
//        private final CommentRepository commentRepository;
//
//        @Transactional
//        public void save() {
//            PasswordEncoder delegatingPasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
//
//            //== MEMBER 저장 ==//
//            userRepository.save(UserEntity.builder()
//                    .username("username1")
//                    .password(delegatingPasswordEncoder
//                            .encode("1234567890"))
//                    .nickname("USER1")
//                    .role(Role.USER)
//                    .build());
//
//            userRepository.save(UserEntity.builder()
//                    .username("username2")
//                    .password(delegatingPasswordEncoder.encode("1234567890"))
//                    .nickname("USER2")
//                    .role(Role.USER)
//                    .build());
//
//            userRepository.save(UserEntity.builder()
//                    .username("username3")
//                    .password(delegatingPasswordEncoder.encode("1234567890"))
//                    .nickname("USER3")
//                    .role(Role.USER)
//                    .build());
//
//            UserEntity user1 = userRepository.findByUsername("username1").get();
//            UserEntity user2 = userRepository.findByUsername("username2").get();
//            UserEntity user3 = userRepository.findByUsername("username3").get();
//
//            System.out.println("===========================================");
//            System.out.println("user1 = " + user1.getId());
//            System.out.println("user2 = " + user2.getId());
//            System.out.println("user3 = " + user3.getId());
//            System.out.println("===========================================");
//
//            for(int i = 0; i<=50; i++ ){
//                BoardEntity board = BoardEntity.builder().title(format("게시글 %s", i)).content(format("내용 %s", i)).build();
//                board.confirmWriter(userRepository.findById((long) (i % 3 + 1)).orElse(null)); //얘가 1, 2, 3이 되는데 앞서 3개만 만들었는데 왜 안되냐
//                boardRepository.save(board);
//            }
//
//            for(int i = 1; i<=150; i++ ){
//                CommentEntity comment = CommentEntity.builder().content("댓글" + i).build();
//                comment.confirmWriter(userRepository.findById((long) (i % 3 + 1)).orElse(null));
//
//                comment.confirmBoard(boardRepository.findById(parseLong(valueOf(i%50 + 1))).orElse(null));
//                commentRepository.save(comment);
//            }
//
//
//            commentRepository.findAll().stream().forEach(comment -> {
//
//                for(int i = 1; i<=50; i++ ){
//                    CommentEntity reComment = CommentEntity.builder().content("대댓글" + i).build();
//                    reComment.confirmWriter(userRepository.findById((long) (i % 3 + 1)).orElse(null));
//
//                    reComment.confirmBoard(comment.getBoard());
//                    reComment.confirmParent(comment);
//                    commentRepository.save(reComment);
//                }
//
//            });
//        }
//    }
//}