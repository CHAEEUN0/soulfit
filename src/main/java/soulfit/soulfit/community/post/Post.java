package soulfit.soulfit.community.post;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.like.PostLike;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Builder
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private PostCategory postCategory;

    @Lob
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poster_id")
    private UserAuth poster;

    @Builder.Default
    private int likeCount = 0;
    @Builder.Default
    private int bookmarkCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostBookmark> postBookmarks = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    public void addLike(PostLike like) {
        this.postLikes.add(like);
        like.setPost(this);
        this.likeCount++;
    }

    public void removeLike(PostLike like) {
        this.postLikes.remove(like);
        like.setPost(null);
        this.likeCount--;
    }

    public void addBookmark(PostBookmark bookmark){
        this.postBookmarks.add(bookmark);
        bookmark.setPost(this);
        this.bookmarkCount++;
    }

    public void removeBookmark(PostBookmark bookmark){
        this.getPostBookmarks().remove(bookmark);
        bookmark.setPost(null);
        this.bookmarkCount--;
    }


    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
