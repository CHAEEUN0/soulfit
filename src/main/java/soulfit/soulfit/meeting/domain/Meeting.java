package soulfit.soulfit.meeting.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.dto.MeetingRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Meeting {

    @Id @GeneratedValue
    @Column(name = "meeting_id")
    private Long id;

    private String title;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id")
    private UserAuth host;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Embedded
    private Location location;

    private int fee;

    @OneToMany(mappedBy = "meeting")
    @Builder.Default
    private List<MeetingParticipant> meetingParticipants = new ArrayList<>();


    private LocalDateTime meetingTime;

    private LocalDateTime recruitDeadline;
    private int maxParticipants;

    private int currentParticipants;

    @Enumerated(EnumType.STRING)
    private MeetingStatus status;

    @CreatedDate
    @Column(updatable = false)
    protected LocalDateTime createdAt;

    @LastModifiedDate
    protected LocalDateTime updatedAt;


    public static Meeting createMeeting(MeetingRequest request, UserAuth host) {
        Meeting meeting = request.toEntity();
        host.addMeeting(meeting);
        meeting.setStatus(MeetingStatus.OPEN);
        meeting.setCurrentParticipants(0);
        return meeting;
    }


    public void update(MeetingRequest request) {
        if (request.getTitle() != null) this.title = request.getTitle();
        if (request.getDescription() != null) this.description = request.getDescription();
        if (request.getCategory() != null) this.category = request.getCategory();
        if (request.getLocation() != null) this.location = request.getLocation();
        if (request.getMeetingTime() != null) this.meetingTime = request.getMeetingTime();
        if (request.getFee() != null) this.fee = request.getFee();
        if (request.getMaxParticipants() != null) this.maxParticipants = request.getMaxParticipants();
    }

    public void setCurrentParticipants(int currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public void setStatus(MeetingStatus status) {
        this.status = status;
    }

    public void setHost(UserAuth host) {
        this.host = host;
    }
}
