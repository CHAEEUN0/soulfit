package soulfit.soulfit.notification.controller;


import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.util.JwtUtil;
import soulfit.soulfit.notification.domain.Notification;
import soulfit.soulfit.notification.domain.NotificationType;
import soulfit.soulfit.notification.repository.NotificationRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class notificationcontrollertest2 {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JwtUtil jwtTokenProvider;

    @Autowired
    private EntityManager em;

    private UserAuth testUser;
    private String jwtToken;

    @BeforeEach
    void setup() {
        testUser = new UserAuth();
        testUser.setId(null);
        testUser.setUsername("testuser");
        testUser.setPassword("samplepwd");
        testUser.setEmail("testman@example.con");
        em.persist(testUser);
        em.flush();

        jwtToken = jwtTokenProvider.generateToken(testUser.getUsername());

        notificationRepository.save(Notification.builder()
                .receiver(testUser)
                .type(NotificationType.TYPE_A)
                .title("테스트 알림")
                .body("본문 내용")
                .isRead(false)
                .build());
    }

    @Test
    @DisplayName("JWT 인증 후 알림 목록 조회")
    void getNotificationsWithJwt() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].title", is("테스트 알림")));
    }

    @Test
    @DisplayName("JWT 인증 후 단건 읽음 처리")
    void markAsReadWithJwt() throws Exception {
        Notification notification = notificationRepository.findAll().get(0);

        mockMvc.perform(post("/api/notifications/" + notification.getId() + "/read")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        Notification updated = notificationRepository.findById(notification.getId()).get();
        assert(updated.isRead());
    }

    @Test
    @DisplayName("JWT 인증 후 전체 읽음 처리")
    void markAllAsReadWithJwt() throws Exception {
        mockMvc.perform(post("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        Notification notification = notificationRepository.findAll().get(0);
        assert(notification.isRead());
    }

    @Test
    @DisplayName("JWT 인증 후 단건 삭제")
    void deleteNotificationWithJwt() throws Exception {
        Notification notification = notificationRepository.findAll().get(0);

        mockMvc.perform(delete("/api/notifications/" + notification.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        boolean exists = notificationRepository.existsById(notification.getId());
        assert(!exists);
    }
}
