package soulfit.soulfit.notification.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String body;
    private Long targetId;
    private boolean isRead;
    private String createdAt;
}

