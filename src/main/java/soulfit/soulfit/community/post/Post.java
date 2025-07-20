package soulfit.soulfit.community.post;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private PostCategory postCategory;

    @Lob
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poster_id")
    private UserAuth poster;

    private int likeCount;
    private int bookmarkCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostBookmark> postBookmarks = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Post(Long id, String content, UserAuth poster, PostCategory category) {
        this.id = id;
        this.content = content;
        this.poster = poster;
        this.postCategory = category;
        this.likeCount = 0;
        this.bookmarkCount = 0;
    }

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
        this.getPostBookmarks().remove(this);
        bookmark.setPost(null);
        this.bookmarkCount--;
    }


    public void updateContent(String content) {
        this.content = content;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
