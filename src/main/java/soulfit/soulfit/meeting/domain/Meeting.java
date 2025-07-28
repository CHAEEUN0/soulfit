package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.dto.MeetingRequestDto;
import soulfit.soulfit.meeting.dto.MeetingUpdateRequestDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Meeting {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meeting_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Embedded
    @Column(nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private UserAuth host;

    @Column(nullable = false)
    private int fee;

    private String feeDescription;


    @ElementCollection
    @CollectionTable(name="meeting_supply", joinColumns = @JoinColumn(name="meeting_id"))
    @Column(name="supply")
    @Builder.Default
    private List<String> supplies = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "meeting_schedule", joinColumns = @JoinColumn(name="meeting_id"))
    @Column(name = "schedule")
    @Builder.Default
    private List<String> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MeetingImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MeetingReview> reviews = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "meeting_keyword",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    @Builder.Default
    private Set<Keyword> keywords = new HashSet<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MeetingBookmark> meetingBookmarks = new ArrayList<>();

    @Column(nullable = false)
    private boolean canPickup;


    @OneToMany(mappedBy = "meeting")
    @Builder.Default
    private List<MeetingParticipant> meetingParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MeetingQuestion> questions = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime meetingTime;
    private int duration;

    private LocalDateTime recruitDeadline;
    private int maxParticipants;

    private int currentParticipants;

    @Enumerated(EnumType.STRING)
    private MeetingStatus meetingStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;
    private int bookmarkCount;


    public void addBookmark(MeetingBookmark bookmark){
        this.meetingBookmarks.add(bookmark);
        bookmark.setMeeting(this);
        this.bookmarkCount++;
    }

    public void removeBookmark(MeetingBookmark bookmark){
        this.getMeetingBookmarks().remove(bookmark);
        bookmark.setMeeting(null);
        this.bookmarkCount--;
    }


    public static Meeting createMeeting(MeetingRequestDto request, UserAuth host) {
        Meeting meeting = request.toEntity();
        meeting.setHost(host);
        host.addMeeting(meeting);
        meeting.setMeetingStatus(MeetingStatus.OPEN);
        meeting.setCurrentParticipants(0);
        return meeting;
    }


    public void update(MeetingUpdateRequestDto dto) {
        if (dto.getTitle() != null) this.title = dto.getTitle();
        if (dto.getDescription() != null) this.description = dto.getDescription();
        if (dto.getCategory() != null) this.category = dto.getCategory();
        if (dto.getLocation() != null) this.location = dto.getLocation();
        if (dto.getFee() != null) this.fee = dto.getFee();
        if (dto.getFeeDescription() != null) this.feeDescription = dto.getFeeDescription();
        if (dto.getCanPickup() != null) this.canPickup = dto.getCanPickup();
        if (dto.getMeetingTime() != null) this.meetingTime = dto.getMeetingTime();
        if (dto.getDuration() != null) this.duration = dto.getDuration();
        if (dto.getMaxParticipants() != null) this.maxParticipants = dto.getMaxParticipants();
        if (dto.getMeetingStatus()!= null) this.meetingStatus = dto.getMeetingStatus();

        if (dto.getSupplies() != null) {
            this.supplies.clear();
            this.supplies.addAll(dto.getSupplies());
        }

        if (dto.getSchedules() != null) {
            this.schedules.clear();
            this.schedules.addAll(dto.getSchedules());
        }
    }

    public void setCurrentParticipants(int currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public void setMeetingStatus(MeetingStatus meetingStatus) {
        this.meetingStatus = meetingStatus;
    }

    public void setHost(UserAuth host) {
        this.host = host;
    }

    public void addParticipant() {
        if (this.currentParticipants >= this.maxParticipants) {
            throw new IllegalStateException("Meeting has reached its maximum participants.");
        }
        this.currentParticipants++;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setKeywords(Set<Keyword> keywords) {
        this.keywords = keywords;
    }
}
