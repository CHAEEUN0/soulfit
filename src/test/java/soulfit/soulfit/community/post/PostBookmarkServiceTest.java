package soulfit.soulfit.community.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostBookmarkServiceTest {

    @Autowired
    PostBookmarkService bookmarkService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostBookmarkRepository bookmarkRepository;

    @Test
    void 여러유저_북마크(){
        UserAuth user1 = new UserAuth("user1", "test1", "user1@test.com");
        UserAuth user2 = new UserAuth("user2", "test2", "user2@test.com");

        userRepository.save(user1);
        userRepository.save(user2);

        Post post = Post.builder()
                .content("북마크 테스트")
                .poster(user1)
                .postCategory(PostCategory.MEETING_REVIEW)
                .build();

        postRepository.save(post);
        Long postId = post.getId();

        bookmarkService.bookmarkOrUnBookmark(postId, user1);
        bookmarkService.bookmarkOrUnBookmark(postId, user2);

        assertEquals(2, bookmarkRepository.findAll().size());
        assertTrue(bookmarkRepository.findByPostAndUser(post, user1).isPresent());
        assertTrue(bookmarkRepository.findByPostAndUser(post, user2).isPresent());

        bookmarkService.bookmarkOrUnBookmark(post.getId(), user1);

        assertEquals(1, bookmarkRepository.findAll().size());
        assertFalse(bookmarkRepository.findByPostAndUser(post, user1).isPresent());
        assertTrue(bookmarkRepository.findByPostAndUser(post, user2).isPresent());

        bookmarkService.bookmarkOrUnBookmark(post.getId(), user2);
        assertEquals(0, bookmarkRepository.findAll().size());
    }

    @Test
    void 유저북마크_조회() throws InterruptedException {
        UserAuth user = new UserAuth("test", "1234", "test@test.com");
        userRepository.save(user);

        Post post1 = Post.builder().content("글1").poster(user).postCategory(PostCategory.MEETING_REVIEW).build();
        Post post2 = Post.builder().content("글2").poster(user).postCategory(PostCategory.QUICK_MEETING).build();
        postRepository.save(post1);
        postRepository.save(post2);

        bookmarkService.bookmarkOrUnBookmark(post1.getId(), user);
        Thread.sleep(10);
        bookmarkService.bookmarkOrUnBookmark(post2.getId(), user);

        Pageable pageable = Pageable.ofSize(10);
        Page<Post> result = bookmarkService.getBookmarkedPostsByUser(user, pageable);

        List<Post> postList = result.getContent();
        assertEquals(2, postList.size());
        assertEquals(post2.getId(), postList.get(0).getId());
        assertEquals(post1.getId(), postList.get(1).getId());
    }
}