package soulfit.soulfit.community.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT DISTINCT c FROM Comment c " +
            "LEFT JOIN FETCH c.children " +
            "WHERE c.post.id = :postId AND c.parent IS NULL " +
            "ORDER BY c.likeCount DESC, c.createdAt DESC")
    List<Comment> findRootCommentsOrderByLikes(@Param("postId") Long postId);

}
