package soulfit.soulfit.config.initializer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.matching.conversation.domain.ConversationRequest;
import soulfit.soulfit.matching.conversation.repository.ConversationRequestRepository;

import java.time.LocalDateTime;

@Component
@Profile("!test")
@Order(4) // UserInitializer, ProfileInitializer 다음에 실행
public class ConversationRequestInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRequestRepository conversationRequestRepository;

    @Override
    public void run(String... args) throws Exception {
        if (conversationRequestRepository.count() == 0) {
            // 'user' 와 'admin' 사용자를 찾습니다.
            UserAuth user = userRepository.findByUsername("user").orElseThrow(
                () -> new RuntimeException("User 'user' not found. Please run UserInitializer first.")
            );
            UserAuth admin = userRepository.findByUsername("admin").orElseThrow(
                () -> new RuntimeException("User 'admin' not found. Please run UserInitializer first.")
            );
            UserAuth user2 = userRepository.findByUsername("user2").orElseThrow(
                () -> new RuntimeException("User 'user2' not found. Please run UserInitializer first.")
            );

            // 1. 'user'가 'admin'에게 보내는 대화 요청 생성
            ConversationRequest requestFromUser = ConversationRequest.builder()
                    .fromUser(user)
                    .toUser(admin)
                    .message("안녕하세요! 같이 운동하고 싶어서 대화 요청 보냅니다.")
                    .build();
            conversationRequestRepository.save(requestFromUser);

            // 2. 'admin'이 'user'에게 보내는 대화 요청 생성
            ConversationRequest requestFromAdmin = ConversationRequest.builder()
                    .fromUser(admin)
                    .toUser(user)
                    .message("안녕하세요. 프로필 잘 봤습니다. 대화 나눠보고 싶어요.")
                    .build();
            conversationRequestRepository.save(requestFromAdmin);

            // 3. 'user2'가 'user'에게 보내는 대화 요청 생성
            ConversationRequest requestFromUser2 = ConversationRequest.builder()
                    .fromUser(user2)
                    .toUser(user)
                    .message("안녕하세요 user님, 프로필 보고 연락드렸습니다.")
                    .build();
            conversationRequestRepository.save(requestFromUser2);


            System.out.println("✅ Sample conversation requests created: 'user' -> 'admin', 'admin' -> 'user', and 'user2' -> 'user'.");
        }
    }
}
