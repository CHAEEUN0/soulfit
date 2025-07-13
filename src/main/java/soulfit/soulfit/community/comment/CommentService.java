package soulfit.soulfit.community.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.comment.dto.CommentRequestDto;
import soulfit.soulfit.community.post.Post;
import soulfit.soulfit.community.post.PostRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public Comment createComment(Long postId, CommentRequestDto requestDto, UserAuth user){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Comment parent = null;
        if (requestDto.getParentId() != null){
             parent = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("부모 댓글 없음"));

             if (parent.getParent() != null){
                 throw new RuntimeException("대댓글까지만 가능");
             }

        }

        Comment comment = Comment.builder()
                .content(requestDto.getContent())
                .post(post)
                .commenter(user)
                .build();

        if (parent != null) {
            parent.addChild(comment);
        }
        return commentRepository.save(comment);
    }


    public List<Comment> findCommentsByPost(Long postId) {
        return commentRepository.findRootCommentsOrderByLikes(postId);
    }



    @Transactional
    public void deleteComment(Long commentId, UserAuth user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 존재하지 않습니다"));

        if (!comment.getCommenter().getId().equals(user.getId())){
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}
