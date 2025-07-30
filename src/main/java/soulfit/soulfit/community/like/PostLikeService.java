package soulfit.soulfit.community.like;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.post.Post;
import soulfit.soulfit.community.post.PostRepository;

import soulfit.soulfit.notification.domain.NotificationType;
import soulfit.soulfit.notification.service.NotificationService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationService notificationService;

    @Transactional
    public void likeOrUnlikePost(Long postId, @AuthenticationPrincipal UserAuth user){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Optional<PostLike> existing = postLikeRepository.findByPostAndUser(post, user);

        if (existing.isPresent()){
            post.removeLike(existing.get());
            postLikeRepository.delete(existing.get());
        }else{
            PostLike like = PostLike.builder().user(user).build();
            post.addLike(like);
            postLikeRepository.save(like);

            notificationService.sendNotification(
                    user,
                    post.getPoster(),
                    NotificationType.LIKE_POST,
                    "Post Liked",
                    user.getUsername() + " liked your post.",
                    post.getId()
            );
        }

    }
}
