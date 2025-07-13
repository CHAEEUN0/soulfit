package soulfit.soulfit.community.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.comment.dto.CommentRequestDto;
import soulfit.soulfit.community.comment.dto.CommentResponseDto;
import soulfit.soulfit.community.like.CommentLikeService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class CommentController {

    private final CommentService commentService;
    private final CommentLikeService commentLikeService;

    //게시글 댓글 전체 조회
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long postId){
        List<CommentResponseDto> responseDtos = commentService.findCommentsByPost(postId)
                .stream()
                .map(CommentResponseDto::from)
                .toList();
        return ResponseEntity.ok(responseDtos);

    }


    //작성
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(@PathVariable Long postId, @RequestBody CommentRequestDto requestDto, @AuthenticationPrincipal UserAuth user){
        Comment comment = commentService.createComment(postId, requestDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponseDto.from(comment));
    }

    //삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal UserAuth user){
        commentService.deleteComment(commentId, user);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<Void> toggleCommentLike(@PathVariable Long commentId,
                                                  @AuthenticationPrincipal UserAuth user) {
        commentLikeService.likeOrUnlikeComment(commentId, user);
        return ResponseEntity.ok().build();
    }
}
