package soulfit.soulfit.community.post.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostUpdateRequestDto {

    private String content;

    private List<MultipartFile> images;

    @Builder
    public PostUpdateRequestDto(String content, List<MultipartFile> images) {
        this.content = content;
        this.images = images;
    }


}
