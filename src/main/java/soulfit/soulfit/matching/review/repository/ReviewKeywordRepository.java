
package soulfit.soulfit.matching.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.matching.review.domain.ReviewKeyword;

import java.util.List;
import java.util.Set;

public interface ReviewKeywordRepository extends JpaRepository<ReviewKeyword, Long> {
    Set<ReviewKeyword> findByKeywordIn(List<String> keywords);
}
