package soulfit.soulfit.community.comment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentRequestDto {

    private Long parentId;
    private String content;
}
