package soulfit.soulfit.community.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.authentication.entity.UserAuth;


import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);

    Page<Post> findAllByPosterOrderByCreatedAtDesc(UserAuth poster, Pageable pageable);

    Page<Post> findByPostCategoryOrderByCreatedAtDesc(PostCategory category, Pageable pageable);
    Page<Post> findByPostCategoryOrderByLikeCountDesc(PostCategory category, Pageable pageable);

    int countByPoster(UserAuth user);


}

