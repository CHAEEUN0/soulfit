package soulfit.soulfit.community.post.dto;

import lombok.Builder;
import lombok.Getter;
import org.w3c.dom.stylesheets.LinkStyle;
import soulfit.soulfit.community.post.Post;
import soulfit.soulfit.community.post.PostCategory;
import soulfit.soulfit.community.post.PostImage;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class PostResponseDto {

    private Long id;
    private String content;
    private String posterUsername;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;
    private PostCategory postCategory;


    @Builder
    public PostResponseDto(Long id, String content, String posterUsername, int likeCount, LocalDateTime createdAt, LocalDateTime updatedAt, List<String> imageUrls, PostCategory postCategory) {
        this.id = id;
        this.content = content;
        this.posterUsername = posterUsername;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.imageUrls = imageUrls;
        this.postCategory = postCategory;
    }



    public static PostResponseDto from(Post post) {

        List<String> imageUrls = post.getImages() != null
                ? post.getImages().stream()
                .map(PostImage::getImageUrl)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return PostResponseDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .posterUsername(post.getPoster().getUsername())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .imageUrls(imageUrls)
                .postCategory(post.getPostCategory())
                .build();
    }
}
