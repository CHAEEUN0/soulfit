package soulfit.soulfit.community.like;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.community.post.Post;
import soulfit.soulfit.community.post.PostCategory;
import soulfit.soulfit.community.post.PostRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostLikeServiceTest {

    @Autowired
    PostLikeService postLikeService;
    @Autowired
    PostLikeRepository postLikeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PostRepository postRepository;

    @Test
    void 여러유저_좋아요(){
        UserAuth user1 = new UserAuth("user1", "test1", "user1@test.com");
        UserAuth user2 = new UserAuth("user2", "test2", "user2@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        Post post = Post.builder()
                .content("좋아요 테스트")
                .poster(user1)
                .postCategory(PostCategory.MEETING_REVIEW)
                .build();

        postRepository.save(post);
        Long postId = post.getId();

        postLikeService.likeOrUnlikePost(postId, user1);
        postLikeService.likeOrUnlikePost(postId, user2);

        assertEquals(2, postLikeRepository.findAll().size());
        assertTrue(postLikeRepository.findByPostAndUser(post, user1).isPresent());
        assertTrue(postLikeRepository.findByPostAndUser(post, user2).isPresent());

        postLikeService.likeOrUnlikePost(postId, user1);
        assertEquals(1, postLikeRepository.findAll().size());
        assertTrue(postLikeRepository.findByPostAndUser(post, user1).isEmpty());
        assertTrue(postLikeRepository.findByPostAndUser(post, user2).isPresent());

        postLikeService.likeOrUnlikePost(postId, user2);
        assertEquals(0, postLikeRepository.findAll().size());
    }

}