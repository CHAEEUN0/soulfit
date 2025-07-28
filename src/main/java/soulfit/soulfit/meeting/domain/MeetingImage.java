package soulfit.soulfit.meeting.domain;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
public class MeetingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    private String imageUrl;
    private String imageKey;

    @Column(name = "image_order")
    private int order;

    @Builder
    public MeetingImage(Meeting meeting, String imageUrl, String imageKey, int order) {
        this.meeting = meeting;
        this.imageUrl = imageUrl;
        this.imageKey = imageKey;
        this.order = order;
    }
}