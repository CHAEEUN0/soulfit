package soulfit.soulfit.community.post;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostBookmarkService {

    private final PostRepository postRepository;
    private final PostBookmarkRepository postBookmarkRepository;

    @Transactional
    public void bookmarkOrUnbookmark(Long postId, @AuthenticationPrincipal UserAuth user){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));

        Optional<PostBookmark> existing = postBookmarkRepository.findByPostAndUser(post, user);

        if (existing.isPresent()){
            post.removeBookmark(existing.get());
            postBookmarkRepository.delete(existing.get());
        }else{
            PostBookmark bookmark = PostBookmark.builder().user(user).build();
            post.addBookmark(bookmark);
            postBookmarkRepository.save(bookmark);

        }

    }

    @Transactional(readOnly = true)
    public Page<Post> getBookmarkedPostsByUser(UserAuth user, Pageable pageable) {
        return postBookmarkRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(PostBookmark::getPost);
    }
}
