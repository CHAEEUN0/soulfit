package soulfit.soulfit.community.post.dto;

import lombok.Builder;
import lombok.Getter;
import soulfit.soulfit.community.post.Post;

import java.time.LocalDateTime;

@Getter
public class PostResponseDto {

    private Long id;
    private String content;
    private String posterUsername;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @Builder
    public PostResponseDto(Long id, String content, String posterUsername, int likeCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.content = content;
        this.posterUsername = posterUsername;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PostResponseDto from(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .posterUsername(post.getPoster().getUsername())
                .likeCount(post.getLikeCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
