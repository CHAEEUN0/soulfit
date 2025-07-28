package soulfit.soulfit.community.post;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.common.S3Uploader;
import soulfit.soulfit.community.post.dto.PostCreateRequestDto;
import soulfit.soulfit.community.post.dto.PostUpdateRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostImageService postImageService;

    @Transactional(readOnly = true)
    public Page<Post> findAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> getPopular(PostCategory category, Pageable pageable) {
        return postRepository.findByPostCategoryOrderByLikeCountDesc(category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Post> getLatest(PostCategory category, Pageable pageable) {
        return postRepository.findByPostCategoryOrderByCreatedAtDesc(category, pageable);
    }

    @Transactional(readOnly = true)
    public Post findPostById(Long postId){
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
    }

    //유저가 쓴글 목록
    @Transactional(readOnly = true)
    public Page<Post> findPostByUser(UserAuth userAuth, Pageable pageable){
        return postRepository.findAllByPosterOrderByCreatedAtDesc(userAuth, pageable);
    }



    @Transactional
    public Post createPost(PostCreateRequestDto requestDto, UserAuth user){
        Post post = Post.builder().
                content(requestDto.getContent())
                .poster(user)
                .postCategory(requestDto.getPostCategory())
                .build();

        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            List<PostImage> postImages = postImageService.uploadImages(requestDto.getImages(), post);
            post.getImages().addAll(postImages);
        }

        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(PostUpdateRequestDto requestDto, Long postId, UserAuth user){

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        if (!post.getPoster().getId().equals(user.getId())){
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        postImageService.deleteImages(post.getImages());
        post.getImages().clear();

        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            List<PostImage> postImages = postImageService.uploadImages(requestDto.getImages(), post);
            post.getImages().addAll(postImages);
        }
        post.setUpdatedAt(LocalDateTime.now());

        if (requestDto.getContent()!= null && !requestDto.getContent().isBlank()) post.updateContent(requestDto.getContent());

        return post;
    }


    @Transactional
    public void deletePost(Long postId, UserAuth user){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
        if (!post.getPoster().getId().equals(user.getId())){
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        postImageService.deleteImages(post.getImages());
        postRepository.deleteById(postId);
    }

    //모임리뷰 전용
    @Transactional
    public Post createReviewPost(String content, List<MultipartFile> images, UserAuth user) {
        Post post = Post.builder().
                content(content)
                .poster(user)
                .postCategory(PostCategory.MEETING_REVIEW)
                .build();

        if (images != null && !images.isEmpty()){
            List<PostImage> postImages = postImageService.uploadImages(images, post);
            post.getImages().addAll(postImages);
        }
        return postRepository.save(post);
    }

    @Transactional
    public Post updateReviewPost(String content, List<MultipartFile> images, Long postId, UserAuth user){

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        if (!post.getPoster().getId().equals(user.getId())){
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        postImageService.deleteImages(post.getImages());
        post.getImages().clear();

        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = postImageService.uploadImages(images, post);
            post.getImages().addAll(postImages);
        }
        post.setUpdatedAt(LocalDateTime.now());

        if (content!= null && !content.isBlank()) post.updateContent(content);

        return post;
    }



}
