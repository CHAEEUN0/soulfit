package soulfit.soulfit.community.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.common.S3Uploader;
import soulfit.soulfit.community.post.dto.PostCreateRequestDto;
import soulfit.soulfit.community.post.dto.PostUpdateRequestDto;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PostServicesTest {

    @Autowired
    PostService postService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    S3Uploader s3Uploader;

    @Autowired
    UserRepository userRepository;

    @Test
    void 게시글_이미지_등록() throws Exception {
        UserAuth user = new UserAuth("tester", "pw", "test@test.com");

        userRepository.save(user);

        InputStream fis = getClass().getResourceAsStream("/test.png");
        MockMultipartFile multipartFile = new MockMultipartFile("image", "test.png", "image/png", fis);

        PostCreateRequestDto createDto = PostCreateRequestDto.builder()
                .content("테스트")
                .postCategory(PostCategory.MEETING_REVIEW)
                .images(List.of(multipartFile))
                .build();

        Post post = postService.createPost(createDto, user);
        assertNotNull(post.getId());
        assertFalse(post.getImages().isEmpty());
        System.out.println("imageUrl = " + post.getImages().get(0).getImageUrl());


    }

    @Test
    void 게시글_다중이미지_등록() throws Exception {
        UserAuth user = new UserAuth("test", "1234", "test@Test.com");
        userRepository.save(user);

        InputStream fis1 = getClass().getResourceAsStream("/test1.jpeg");
        InputStream fis2 = getClass().getResourceAsStream("/test2.png");

        MockMultipartFile multipartFile1 = new MockMultipartFile(
                "images", "test1.jpeg", "image/jpeg", fis1);
        MockMultipartFile multipartFile2 = new MockMultipartFile(
                "images", "test2.png", "image/png", fis2);


        List<MultipartFile> files = List.of(multipartFile1, multipartFile2);

        PostCreateRequestDto dto = PostCreateRequestDto.builder()
                .content("여러 장 테스트")
                .postCategory(PostCategory.QUICK_MEETING)
                .images(files)
                .build();


        Post post = postService.createPost(dto, user);

        assertEquals(2, post.getImages().size());
        post.getImages().forEach(img -> {
            System.out.println("imgUrl = " + img.getImageUrl());
        });

    }

    @Test
    void 게시글_수정() throws Exception {
        UserAuth user = new UserAuth("test", "1234", "test@Test.com");
        userRepository.save(user);

        PostCreateRequestDto createDto = PostCreateRequestDto.builder().content("처음글").build();
        Post post = postService.createPost(createDto, user);
        Long postId = post.getId();

        InputStream fis1 = getClass().getResourceAsStream("/test1.jpeg");
        InputStream fis2 = getClass().getResourceAsStream("/test2.png");

        MockMultipartFile multipartFile1 = new MockMultipartFile(
                "images", "test1.jpeg", "image/jpeg", fis1);
        MockMultipartFile multipartFile2 = new MockMultipartFile(
                "images", "test2.png", "image/png", fis2);

        PostUpdateRequestDto updateDto = PostUpdateRequestDto.builder()
                .content("수정글")
                .images(List.of(multipartFile1, multipartFile2))
                .build();

        Post updatedPost = postService.updatePost(updateDto, postId, user);

        assertEquals("수정글", updatedPost.getContent());
        assertEquals(2, updatedPost.getImages().size());
        updatedPost.getImages().forEach(img -> {
            System.out.println("imgUrl = " + img.getImageUrl());
        });


    }


}

