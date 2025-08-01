package soulfit.soulfit.meeting.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import soulfit.soulfit.authentication.entity.QUserAuth;
import soulfit.soulfit.meeting.domain.*;
import soulfit.soulfit.meeting.dto.SearchFilter;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MeetingQueryRepositoryImpl implements MeetingQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Meeting> search(SearchFilter filter, Pageable pageable) {
        QMeeting meeting = QMeeting.meeting;
        QUserAuth userAuth = QUserAuth.userAuth;
        QHostProFile hostProFile = QHostProFile.hostProFile;

        JPAQuery<Meeting> query = queryFactory
                .selectFrom(meeting)
                .join(meeting.host, userAuth)
                .leftJoin(hostProFile).on(hostProFile.userAuth.eq(userAuth))
                .where(
                        cityEq(filter.getCity()),
                        districtEq(filter.getDistrict()),
                        meetingTimeBetween(filter.getStartDate(), filter.getEndDate()),
                        feeGoe(filter.getMinFee()),
                        feeLoe(filter.getMaxFee()),
                        hostRatingGoe(filter.getMinRating()),
                        hostRatingLoe(filter.getMaxRating()),
                        capacityGoe(filter.getMinCapacity()),
                        statusOpen()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .distinct();

        List<Meeting> content = query.fetch();


        JPQLQuery<Long> countQuery = queryFactory
                .select(meeting.count())
                .from(meeting)
                .join(meeting.host, userAuth)
                .leftJoin(hostProFile).on(hostProFile.userAuth.eq(userAuth))
                .where(
                        cityEq(filter.getCity()),
                        districtEq(filter.getDistrict()),
                        meetingTimeBetween(filter.getStartDate(), filter.getEndDate()),
                        feeGoe(filter.getMinFee()),
                        feeLoe(filter.getMaxFee()),
                        hostRatingGoe(filter.getMinRating()),
                        hostRatingLoe(filter.getMaxRating()),
                        capacityGoe(filter.getMinCapacity()),
                        statusOpen()
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression cityEq(String city){
        return (city != null && !city.isEmpty()) ? QMeeting.meeting.location.city.eq(city) : null;
    }

    private BooleanExpression districtEq(String district){
        return (district != null && !district.isEmpty()) ? QMeeting.meeting.location.district.eq(district) : null;
    }

    private BooleanExpression meetingTimeBetween(LocalDate startDate, LocalDate endDate){
        if (startDate != null && endDate != null){
            return QMeeting.meeting.meetingTime.between(
                    startDate.atStartOfDay(),
                    endDate.atTime(23, 59, 59)
            );
        } else if (startDate != null) {
            return QMeeting.meeting.meetingTime.goe(startDate.atStartOfDay());
        } else if (endDate != null) {
            return QMeeting.meeting.meetingTime.loe(endDate.atTime(23, 59, 59));
        }
        return null;
    }

    private BooleanExpression feeGoe(Integer minFee){
        return minFee != null ? QMeeting.meeting.fee.goe(minFee) : null;
    }

    private BooleanExpression feeLoe(Integer maxFee) {
        return maxFee != null ? QMeeting.meeting.fee.loe(maxFee) : null;
    }

    private BooleanExpression hostRatingGoe(Double minRating){
        return minRating != null ? QHostProFile.hostProFile.hostAverageRating.goe(minRating) : null;
    }

    private BooleanExpression hostRatingLoe(Double maxRating){
        return maxRating != null ? QHostProFile.hostProFile.hostAverageRating.loe(maxRating) : null;
    }

    private BooleanExpression capacityGoe(Integer minCapacity){
        return minCapacity != null ? QMeeting.meeting.maxParticipants.goe(minCapacity) : null;
    }

    private BooleanExpression statusOpen(){
        return QMeeting.meeting.meetingStatus.eq(MeetingStatus.OPEN);
    }


}
