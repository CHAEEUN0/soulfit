package soulfit.soulfit.community.post.dto;


import lombok.Builder;
import lombok.Getter;
import soulfit.soulfit.community.post.Post;

@Getter
public class PostRequestDto {

    private String content;

    @Builder
    public PostRequestDto(String content) {
        this.content = content;
    }


}
