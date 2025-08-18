package soulfit.soulfit.authentication.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import soulfit.soulfit.authentication.entity.UserAuth;
import java.util.Set;

public interface UserRepositoryCustom {
    Page<UserAuth> findPotentialSwipeTargets(
        UserAuth currentUser,
        Set<Long> swipedUserIds,
        Double currentUserLatitude,
        Double currentUserLongitude,
        String regionFilter,
        Integer minHeight, Integer maxHeight,
        Integer minAge, Integer maxAge,
        Double maxDistanceInKm,
        String smokingStatusFilter,
        String drinkingStatusFilter,
        Pageable pageable
    );
}
