package soulfit.soulfit.community.post;

import aj.org.objectweb.asm.commons.Remapper;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.post.dto.PostCreateRequestDto;
import soulfit.soulfit.community.post.dto.PostUpdateRequestDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private S3Uploader s3uploader;

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
    public Post createPost(PostCreateRequestDto requestDto, UserAuth userAuth){
        Post post = Post.builder().
                content(requestDto.getContent())
                .poster(userAuth)
                .category(requestDto.getCategory())
                .build();

        List<MultipartFile> images = requestDto.getImages();

        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = uploadAndCreatePostImages(images, post);
            post.getImages().addAll(postImages);
        }

        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(PostUpdateRequestDto requestDto, Long postId, UserAuth userAuth){

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        if (!post.getPoster().getId().equals(userAuth.getId())){
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        for (PostImage image : post.getImages()) {
            s3uploader.delete(image.getImageKey());
        }
        post.getImages().clear();

        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            List<PostImage> postImages = uploadAndCreatePostImages(requestDto.getImages(), post);
            post.getImages().addAll(postImages);
        }

        if (requestDto.getContent()!= null && !requestDto.getContent().isBlank()) post.updateContent(requestDto.getContent());

        return post;
    }

    private List<PostImage> uploadAndCreatePostImages(List<MultipartFile> images, Post post) {
        List<PostImage> postImages = new ArrayList<>();
        for (MultipartFile image : images) {
            String key = createKeyName(image.getOriginalFilename());
            String imageUrl = s3uploader.upload(image, key);

            postImages.add(PostImage.builder()
                    .post(post)
                    .imageUrl(imageUrl)
                    .imageKey(key)
                    .build());

        }
        return postImages;
    }

    private String createKeyName(String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")); // 예: ".jpg"
        }
        return "post/" + UUID.randomUUID() + ext;
    }

    @Transactional
    public void deletePost(Long postId, UserAuth userAuth){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
        if (!post.getPoster().getId().equals(userAuth.getId())){
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        for (PostImage image : post.getImages()) {
            s3uploader.delete(image.getImageUrl());
        }
        postRepository.deleteById(postId);
    }


}
