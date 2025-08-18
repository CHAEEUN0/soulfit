package soulfit.soulfit.matching.swipe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.swipe.domain.Swipe;
import soulfit.soulfit.matching.swipe.domain.SwipeType;

import java.util.List;
import java.util.Optional;

public interface SwipeRepository extends JpaRepository<Swipe, Long> {
    Optional<Swipe> findBySwiperAndSwipedAndType(UserAuth swiper, UserAuth swiped, SwipeType type);

    // 특정 사용자가 'LIKE'한 모든 Swipe 기록을 조회
    List<Swipe> findBySwiperAndType(UserAuth swiper, SwipeType type);

    // 특정 사용자를 'LIKE'한 모든 Swipe 기록을 조회
    List<Swipe> findBySwipedAndType(UserAuth swiped, SwipeType type);

    List<Swipe> findBySwiper(UserAuth swiper);
}
