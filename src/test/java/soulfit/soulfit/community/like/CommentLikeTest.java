package soulfit.soulfit.community.like;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.community.comment.Comment;
import soulfit.soulfit.community.comment.CommentRepository;
import soulfit.soulfit.community.comment.CommentService;
import soulfit.soulfit.community.comment.dto.CommentRequestDto;
import soulfit.soulfit.community.post.Post;
import soulfit.soulfit.community.post.PostCategory;
import soulfit.soulfit.community.post.PostRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentLikeTest {

    @Autowired
    CommentRepository commentRepository;
    @Autowired
    CommentLikeService commentLikeService;
    @Autowired
    CommentLikeRepository commentLikeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;

    @Test
    void 여러유저_댓글_좋아요(){
        UserAuth user1 = new UserAuth("user1", "test1", "user1@test.com");
        UserAuth user2 = new UserAuth("user2", "test2", "user2@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        Post post = Post.builder()
                .content("댓글 좋아요 테스트")
                .poster(user1)
                .postCategory(PostCategory.MEETING_REVIEW)
                .build();

        postRepository.save(post);
        Long postId = post.getId();

        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .commenter(user1)
                .build();
        commentRepository.save(comment);

        commentLikeService.likeOrUnlikeComment(comment.getId(), user1);
        commentLikeService.likeOrUnlikeComment(comment.getId(), user2);

        assertEquals(2, commentLikeRepository.findAll().size());
    }

    @Test
    void 여러유저_대댓글_좋아요(){
        UserAuth user1 = new UserAuth("user1", "test1", "user1@test.com");
        UserAuth user2 = new UserAuth("user2", "test2", "user2@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        Post post = Post.builder()
                .content("댓글 좋아요 테스트")
                .poster(user1)
                .postCategory(PostCategory.MEETING_REVIEW)
                .build();

        postRepository.save(post);
        Long postId = post.getId();

        Comment comment = Comment.builder()
                .content("테스트 댓글")
                .post(post)
                .commenter(user1)
                .build();
        commentRepository.save(comment);

        Comment childComment = Comment.builder()
                .content("테스트 대댓글")
                .post(post)
                .commenter(user2)
                .build();
        commentRepository.save(childComment);

        commentLikeService.likeOrUnlikeComment(comment.getId(), user1);
        commentLikeService.likeOrUnlikeComment(childComment.getId(), user1);
        commentLikeService.likeOrUnlikeComment(childComment.getId(), user2);

        assertEquals(3, commentLikeRepository.findAll().size());
    }


}