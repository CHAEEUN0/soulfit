package soulfit.soulfit.meeting.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.post.PostImage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MeetingReview {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserAuth user;


    @Column(nullable = false)
    private double meetingRating;

    @Column(nullable = false)
    private double hostRating;

    private String content;


    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @ElementCollection
    @CollectionTable(name = "meeting_review_image", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> postImageUrls = new ArrayList<>();
    private Long postId;



    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }


    public void setPostImageUrls(List<String> postImageUrls) {
        this.postImageUrls = postImageUrls;
    }

    public void updateReview(Double meetingRating, Double hostRating, String content) {
        if (meetingRating != null) this.meetingRating = meetingRating;
        if (hostRating != null) this.hostRating = hostRating;
        if (content != null) this.content = content;
    }


}
