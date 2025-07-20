package soulfit.soulfit.community.post.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.community.post.PostCategory;

import java.util.List;

@Getter
public class PostCreateRequestDto {

    @NotNull
    private String content;

    private List<MultipartFile> images;

    private PostCategory category;

    @Builder
    public PostCreateRequestDto(PostCategory category, String content, List<MultipartFile> images) {
        this.category = category;
        this.content = content;
        this.images = images;
    }


}
