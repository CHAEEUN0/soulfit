package soulfit.soulfit.matching.swipe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.swipe.domain.Match;
import soulfit.soulfit.matching.swipe.domain.Swipe;
import soulfit.soulfit.matching.swipe.domain.SwipeType;
import soulfit.soulfit.matching.swipe.dto.MatchResponse;
import soulfit.soulfit.matching.swipe.dto.SwipeRequest;
import soulfit.soulfit.matching.swipe.dto.SwipeUserResponse;
import soulfit.soulfit.matching.swipe.repository.MatchRepository;
import soulfit.soulfit.matching.swipe.repository.SwipeRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SwipeService {

    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    public MatchResponse performSwipe(UserAuth swiper, SwipeRequest swipeRequest) {
        UserAuth swiped = userRepository.findById(swipeRequest.getSwipedUserId())
                .orElseThrow(() -> new IllegalArgumentException("Swiped user not found"));

        // 스와이프 기록 저장
        Swipe swipe = Swipe.builder()
                .swiper(swiper)
                .swiped(swiped)
                .type(swipeRequest.getType())
                .build();
        swipeRepository.save(swipe);

        // 'LIKE'가 아니면 매칭 로직을 실행하지 않음
        if (swipeRequest.getType() != SwipeType.LIKE) {
            return new MatchResponse(false, null);
        }

        // 상대방이 나를 'LIKE' 했는지 확인
        Optional<Swipe> mutualLike = swipeRepository.findBySwiperAndSwipedAndType(swiped, swiper, SwipeType.LIKE);

        // 상대방의 'LIKE' 기록이 없으면 매치가 아님
        if (mutualLike.isEmpty()) {
            return new MatchResponse(false, null);
        }

        // 매치 성공! 매치 정보만 생성
        Match match = createMatch(swiper, swiped);

        // TODO: 알림 서비스가 구현되면 매치 성공 알림을 발송하는 로직 추가
        // notificationService.sendMatchNotification(swiper, swiped);

        return new MatchResponse(true, null); // 채팅방 ID는 null로 반환
    }

    private Match createMatch(UserAuth user1, UserAuth user2) {
        // 매치 정보 저장 (채팅방 생성 로직 제외)
        Match match = Match.builder()
                .user1(user1)
                .user2(user2)
                .chatRoomId(null) // 채팅방 ID는 나중에 다른 프로세스에서 채울 수 있도록 null로 설정
                .build();

        return matchRepository.save(match);
    }

    @Transactional(readOnly = true)
    public List<SwipeUserResponse> getMyLikedUsers(UserAuth currentUser) {
        List<Swipe> myLikes = swipeRepository.findBySwiperAndType(currentUser, SwipeType.LIKE);
        return myLikes.stream()
                .map(Swipe::getSwiped)
                .map(SwipeUserResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SwipeUserResponse> getUsersWhoLikedMe(UserAuth currentUser) {
        List<Swipe> likesOnMe = swipeRepository.findBySwipedAndType(currentUser, SwipeType.LIKE);
        return likesOnMe.stream()
                .map(Swipe::getSwiper)
                .map(SwipeUserResponse::from)
                .collect(Collectors.toList());
    }
}
