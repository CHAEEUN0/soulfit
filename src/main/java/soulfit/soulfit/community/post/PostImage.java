package soulfit.soulfit.community.post;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
public class PostImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private String imageUrl;
    private String imageKey;

    @Column(name = "image_order")
    private int order;

    @Builder
    public PostImage(Post post, String imageUrl, String imageKey, int order) {
        this.post = post;
        this.imageUrl = imageUrl;
        this.imageKey = imageKey;
        this.order = order;
    }
}
