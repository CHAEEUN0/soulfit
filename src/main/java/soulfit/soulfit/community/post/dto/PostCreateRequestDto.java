package soulfit.soulfit.community.post.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.community.post.PostCategory;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostCreateRequestDto {

    @NotNull
    private String content;

    private List<MultipartFile> images;

    private PostCategory postCategory;

    @Builder
    public PostCreateRequestDto(PostCategory postCategory, String content, List<MultipartFile> images) {
        this.postCategory = postCategory;
        this.content = content;
        this.images = images;
    }


}
