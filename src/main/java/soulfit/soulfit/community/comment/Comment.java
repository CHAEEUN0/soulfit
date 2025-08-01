package soulfit.soulfit.community.comment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.like.CommentLike;
import soulfit.soulfit.community.like.PostLike;
import soulfit.soulfit.community.post.Post;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="comment_id")
    private Long id;

    private String content;

    @JoinColumn(name = "commenter_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserAuth commenter;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentLike> commentLikes = new ArrayList<>();

    private int likeCount;


    public void addLike(CommentLike like) {
        this.commentLikes.add(like);
        like.setComment(this);
        this.likeCount++;
    }

    public void removeLike(CommentLike like) {
        this.commentLikes.remove(like);
        like.setComment(null);
        this.likeCount--;
    }


    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void addChild(Comment child) {
        this.children.add(child);
        child.parent = this;
    }

    @Builder
    public Comment(Long id, String content, UserAuth commenter, Post post, Comment parent) {
        this.id = id;
        this.content = content;
        this.commenter = commenter;
        this.post = post;
        this.parent = parent;
    }
}
