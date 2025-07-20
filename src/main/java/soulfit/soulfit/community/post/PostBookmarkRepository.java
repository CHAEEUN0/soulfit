package soulfit.soulfit.community.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.util.Optional;

@Repository
public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    Optional<PostBookmark> findByPostAndUser(Post post, UserAuth userAuth);

    Page<PostBookmark> findByUserOrderByCreatedAtDesc(UserAuth user, Pageable pageable);

}
