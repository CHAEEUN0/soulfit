package soulfit.soulfit.community.comment.dto;

import lombok.Builder;
import lombok.Getter;
import soulfit.soulfit.community.comment.Comment;

import java.util.Comparator;
import java.util.List;

@Getter
@Builder
public class CommentResponseDto {
    private Long id;
    private String content;
    private String username;
    private List<CommentResponseDto> children;


    public static CommentResponseDto from(Comment comment){
        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .username(comment.getCommenter().getUsername())
                .children(comment.getChildren().stream()
                        .sorted(Comparator.comparing(c -> c.getCreatedAt()))
                        .map(CommentResponseDto::from)
                        .toList())
                .build();
    }
}

