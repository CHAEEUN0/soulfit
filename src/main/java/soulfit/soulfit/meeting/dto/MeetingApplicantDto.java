package soulfit.soulfit.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import soulfit.soulfit.meeting.domain.ApprovalStatus;
import soulfit.soulfit.meeting.domain.MeetingParticipant;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingApplicantDto {
    private Long participantId;
    private Long userId;
    private String username;
    private ApprovalStatus approvalStatus;
    private LocalDateTime joinedAt;

    public static MeetingApplicantDto from(MeetingParticipant participant) {
        return new MeetingApplicantDto(
                participant.getId(),
                participant.getUser().getId(),
                participant.getUser().getUsername(),
                participant.getApprovalStatus(),
                participant.getJoinedAt()
        );
    }
}
