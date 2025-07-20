package soulfit.soulfit.community.post;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import soulfit.soulfit.authentication.entity.UserAuth;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_bookmark_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserAuth user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDateTime createdAt;


    @Builder
    public PostBookmark(Post post, UserAuth user) {
        this.post = post;
        this.user = user;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
