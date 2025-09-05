
package soulfit.soulfit.config.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.conversation.domain.ConversationRequest;
import soulfit.soulfit.matching.conversation.repository.ConversationRequestRepository;
import soulfit.soulfit.matching.review.domain.Review;
import soulfit.soulfit.matching.review.domain.ReviewKeyword;
import soulfit.soulfit.matching.review.repository.ReviewKeywordRepository;
import soulfit.soulfit.matching.review.repository.ReviewRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile("!test")
@Order(5)
@RequiredArgsConstructor
public class ReviewInitializer implements CommandLineRunner {

    private final ReviewRepository reviewRepository;
    private final ReviewKeywordRepository reviewKeywordRepository;
    private final UserRepository userRepository;
    private final ConversationRequestRepository conversationRequestRepository;

    @Override
    public void run(String... args) throws Exception {
        if (reviewRepository.count() > 0) {
            return;
        }

        // 1. 샘플 리뷰 키워드 생성
        List<String> keywordTexts = List.of("유머있는", "친절한", "매너있어요", "시간을 잘 지켜요", "리더십 있는", "세심한");
        List<ReviewKeyword> keywords = keywordTexts.stream()
                .map(text -> ReviewKeyword.builder().keyword(text).build())
                .collect(Collectors.toList());
        reviewKeywordRepository.saveAll(keywords);
        Set<ReviewKeyword> savedKeywords = Set.copyOf(reviewKeywordRepository.findAll());

        // 2. 필요 데이터 조회
        UserAuth user = userRepository.findByUsername("user").orElseThrow(() -> new RuntimeException("User 'user' not found."));
        UserAuth admin = userRepository.findByUsername("admin").orElseThrow(() -> new RuntimeException("User 'admin' not found."));
        UserAuth user2 = userRepository.findByUsername("user2").orElseThrow(() -> new RuntimeException("User 'user2' not found."));

        ConversationRequest conversation1 = conversationRequestRepository.findByFromUserAndToUser(user, admin).orElseThrow(() -> new RuntimeException("Conversation from user to admin not found."));
        ConversationRequest conversation2 = conversationRequestRepository.findByFromUserAndToUser(admin, user).orElseThrow(() -> new RuntimeException("Conversation from admin to user not found."));
        ConversationRequest conversation3 = conversationRequestRepository.findByFromUserAndToUser(user2, user).orElseThrow(() -> new RuntimeException("Conversation from user2 to user not found."));

        // 3. 샘플 리뷰 생성
        Review review1 = Review.builder()
                .reviewer(user)
                .reviewee(admin)
                .conversationRequest(conversation1)
                .comment("관리자님, 친절한 답변 감사했습니다!")
                .keywords(filterKeywords(savedKeywords, "친절한", "세심한"))
                .build();

        Review review2 = Review.builder()
                .reviewer(admin)
                .reviewee(user)
                .conversationRequest(conversation2)
                .comment("user님과의 대화는 유쾌했습니다.")
                .keywords(filterKeywords(savedKeywords, "유머있는", "친절한"))
                .build();
        
        Review review3 = Review.builder()
                .reviewer(user2)
                .reviewee(user)
                .conversationRequest(conversation3)
                .comment("덕분에 운동 많이 배우고 갑니다.")
                .keywords(filterKeywords(savedKeywords, "리더십 있는", "매너있어요"))
                .build();

        reviewRepository.saveAll(List.of(review1, review2, review3));

        System.out.println("✅ Sample reviews created.");
    }

    private Set<ReviewKeyword> filterKeywords(Set<ReviewKeyword> keywords, String... texts) {
        List<String> textList = List.of(texts);
        return keywords.stream()
                .filter(k -> textList.contains(k.getKeyword()))
                .collect(Collectors.toSet());
    }
}
