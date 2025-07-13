package soulfit.soulfit.community.like;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.comment.Comment;
import soulfit.soulfit.community.comment.CommentRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Transactional
    public void likeOrUnlikeComment(Long commentId, UserAuth user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글 없음"));

        Optional<CommentLike> existing = commentLikeRepository.findByCommentAndUser(comment, user);

        if (existing.isPresent()) {
            comment.removeLike(existing.get());
            commentLikeRepository.delete(existing.get());
        } else {
            CommentLike like = CommentLike.builder().user(user).build();
            comment.addLike(like);
            commentLikeRepository.save(like);

        }
    }
}