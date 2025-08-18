package soulfit.soulfit.authentication.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static soulfit.soulfit.authentication.entity.QUserAuth.userAuth;
import static soulfit.soulfit.matching.profile.domain.QMatchingProfile.matchingProfile;
import static soulfit.soulfit.profile.domain.QUserProfile.userProfile;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public Page<UserAuth> findPotentialSwipeTargets(
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
    ) {
        BooleanBuilder builder = new BooleanBuilder();

        // 1. Exclude current user
        builder.and(userAuth.id.ne(currentUser.getId()));

        // 2. Exclude already swiped users
        if (swipedUserIds != null && !swipedUserIds.isEmpty()) {
            builder.and(userAuth.id.notIn(swipedUserIds));
        }

        // 3. Exclude users without UserProfile or MatchingProfile
        builder.and(userProfile.isNotNull());
        builder.and(matchingProfile.isNotNull());

        // 4. Apply dynamic filters first (순서 변경)
        applyFilters(builder, regionFilter, minHeight, maxHeight, minAge, maxAge, smokingStatusFilter, drinkingStatusFilter);

        // 3. Build base query with joins and apply all conditions from the builder
        JPAQuery<UserAuth> query = queryFactory
                .selectFrom(userAuth)
                .leftJoin(userAuth.userProfile, userProfile).fetchJoin() // Fetch join for eager loading
                .leftJoin(userAuth.matchingProfile, matchingProfile).fetchJoin() // Fetch join for eager loading
                .where(builder); // <--- 이제 builder에 모든 조건이 포함됩니다.

        // 5. Apply distance filter
//        if (currentUserLatitude != null && currentUserLongitude != null && maxDistanceInKm != null) {
//            builder.and(distanceBetween(
//                    userProfile.latitude, userProfile.longitude,
//                    currentUserLatitude, currentUserLongitude
//            ).lt(maxDistanceInKm)); // Changed from loe to lt
//        }

        // 6. Apply pagination
        List<UserAuth> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 7. Count total elements for pagination metadata
        JPAQuery<Long> countQuery = queryFactory
                .select(userAuth.count())
                .from(userAuth)
                .leftJoin(userAuth.userProfile, userProfile)
                .leftJoin(userAuth.matchingProfile, matchingProfile)
                .where(builder);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // Helper method to apply common filters
    private void applyFilters(
            BooleanBuilder builder,
            String regionFilter,
            Integer minHeight, Integer maxHeight,
            Integer minAge, Integer maxAge,
            String smokingStatusFilter,
            String drinkingStatusFilter
    ) {
        if (regionFilter != null && !regionFilter.isEmpty()) {
            builder.and(userProfile.region.equalsIgnoreCase(regionFilter));
        }

        if (minHeight != null) {
            builder.and(matchingProfile.heightCm.goe(minHeight));
        }
        if (maxHeight != null) {
            builder.and(matchingProfile.heightCm.loe(maxHeight));
        }

        // Age filtering in DB
        if (minAge != null) {
            // 최소 나이에 해당하는 출생 연도 계산 (현재 연도 - 최소 나이)
            int minBirthYear = LocalDate.now().getYear() - minAge;
            builder.and(userProfile.birthDate.year().loe(minBirthYear));
        }
        if (maxAge != null) {
            // 최대 나이에 해당하는 출생 연도 계산 (현재 연도 - 최대 나이)
            int maxBirthYear = LocalDate.now().getYear() - maxAge;
            builder.and(userProfile.birthDate.year().goe(maxBirthYear));
        }

        if (smokingStatusFilter != null && !smokingStatusFilter.isEmpty()) {
            builder.and(matchingProfile.smoking.stringValue().equalsIgnoreCase(smokingStatusFilter));
        }

        if (drinkingStatusFilter != null && !drinkingStatusFilter.isEmpty()) {
            builder.and(matchingProfile.drinking.stringValue().equalsIgnoreCase(drinkingStatusFilter));
        }
    }

    // Helper for age calculation if needed in QueryDSL (more complex)
    // private BooleanExpression ageBetween(Integer minAge, Integer minAge) { ... }

    // Helper for distance calculation in QueryDSL
    private NumberExpression<Double> distanceBetween(
            NumberPath<Double> lat1, NumberPath<Double> lon1,
            Double lat2, Double lon2
    ) {
        return Expressions.numberTemplate(Double.class,
                "FUNCTION('calculate_distance_km', {0}, {1}, {2}, {3})",
                lat1, lon1, lat2, lon2);
    }
}