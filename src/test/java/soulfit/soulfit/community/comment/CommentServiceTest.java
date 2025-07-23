package soulfit.soulfit.community.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.community.comment.dto.CommentRequestDto;
import soulfit.soulfit.community.post.Post;
import soulfit.soulfit.community.post.PostCategory;
import soulfit.soulfit.community.post.PostRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentServiceTest {

    @Autowired
    CommentService commentService;

    @Autowired CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void 댓글_대댓글_등록(){
        UserAuth user1 = new UserAuth("user1", "test1", "user1@test.com");
        UserAuth user2 = new UserAuth("user2", "test2", "user2@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        Post post = Post.builder()
                .content("댓글 테스트")
                .poster(user1)
                .postCategory(PostCategory.MEETING_REVIEW)
                .build();

        postRepository.save(post);
        Long postId = post.getId();

        CommentRequestDto parentDto = CommentRequestDto.builder().content("부모 댓글").build();
        Comment parent = commentService.createComment(postId, parentDto, user1);

        CommentRequestDto childDto = CommentRequestDto.builder().content("대댓글").parentId(parent.getId()).build();
        Comment child = commentService.createComment(postId, childDto, user2);

        assertNotNull(parent.getId());
        assertNotNull(child.getId());

        List<Comment> rootComments = commentService.findCommentsByPost(post.getId());
        assertEquals(1, rootComments.size());
        assertEquals("부모 댓글", rootComments.get(0).getContent());

        assertEquals(1, rootComments.get(0).getChildren().size());
        assertEquals("대댓글", rootComments.get(0).getChildren().get(0).getContent());

    }

    @Test
    void 대대댓글_불가(){
        UserAuth user1 = new UserAuth("user1", "test1", "user1@test.com");
        UserAuth user2 = new UserAuth("user2", "test2", "user2@test.com");
        UserAuth user3 = new UserAuth("user3", "test3", "user3@test.com");

        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        Post post = Post.builder()
                .content("대대댓글 테스트")
                .poster(user1)
                .postCategory(PostCategory.MEETING_REVIEW)
                .build();

        postRepository.save(post);
        
        CommentRequestDto parentDto = CommentRequestDto.builder().content("부모 댓글").build();
        Comment parent = commentService.createComment(post.getId(), parentDto, user1);


        CommentRequestDto childDto = CommentRequestDto.builder().content("대댓글").parentId(parent.getId()).build();
        Comment child = commentService.createComment(post.getId(), childDto, user2);

        //대대댓글 예외
        CommentRequestDto grandChildDto = CommentRequestDto.builder().content("대대댓글").parentId(child.getId()).build();

        assertThrows(RuntimeException.class, () ->
                commentService.createComment(post.getId(), grandChildDto, user3)
        );
    }
}