package soulfit.soulfit.community.post;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.post.dto.PostCreateRequestDto;
import soulfit.soulfit.community.post.dto.PostResponseDto;
import soulfit.soulfit.community.like.PostLikeService;
import soulfit.soulfit.community.post.dto.PostUpdateRequestDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final PostBookmarkService postBookmarkService;



    //전체 조회
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPosts(Pageable pageable){
        Page<PostResponseDto> dtoPage = postService.findAllPosts(pageable)
                .map(PostResponseDto::from);
        return ResponseEntity.ok(dtoPage);
    }

    //카테고리별 인기순
    @GetMapping("/popular")
    public ResponseEntity<Page<PostResponseDto>> getPopularPosts(PostCategory category, Pageable pageable){
        Page<PostResponseDto> dtoPage = postService.getPopular(category, pageable)
                .map(PostResponseDto::from);
        return ResponseEntity.ok(dtoPage);
    }

    //카테고리별 최신순
    @GetMapping("/latest")
    public ResponseEntity<Page<PostResponseDto>> getLatestPosts(PostCategory category, Pageable pageable){
        Page<PostResponseDto> dtoPage = postService.getLatest(category, pageable)
                .map(PostResponseDto::from);
        return ResponseEntity.ok(dtoPage);
    }


    //단건 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId){
        Post post = postService.findPostById(postId);
        return ResponseEntity.ok(PostResponseDto.from(post));
    }

    //유저가 쓴 글 목록
    @GetMapping("/user")
    public ResponseEntity<Page<PostResponseDto>> getUserPosts(@AuthenticationPrincipal UserAuth userAuth, Pageable pageable){
        Page<PostResponseDto> dtoPage= postService.findPostByUser(userAuth, pageable)
                .map(PostResponseDto::from);
        return ResponseEntity.ok(dtoPage);
    }

    //유저 북마크한 글 목록
    @GetMapping("/user/bookmarks")
    public ResponseEntity<Page<PostResponseDto>> getUserBookmarkedPosts(@AuthenticationPrincipal UserAuth user, Pageable pageable){
        Page<PostResponseDto> result = postBookmarkService.getBookmarkedPostsByUser(user, pageable)
                .map(PostResponseDto::from);

        return ResponseEntity.ok(result);
    }
    //작성
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@ModelAttribute @Valid PostCreateRequestDto requestDto, @AuthenticationPrincipal UserAuth userAuth){
        Post post = postService.createPost(requestDto, userAuth);
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponseDto.from(post));
    }

    //수정
    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable Long postId, @ModelAttribute PostUpdateRequestDto requestDto, @AuthenticationPrincipal UserAuth userAuth){
        Post updatedPost = postService.updatePost(requestDto, postId, userAuth);
        return ResponseEntity.ok(PostResponseDto.from(updatedPost));
    }

    //삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, @AuthenticationPrincipal UserAuth userAuth){
        postService.deletePost(postId, userAuth);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<Void> togglePostLike(@PathVariable Long postId,
                                               @AuthenticationPrincipal UserAuth user) {
        postLikeService.likeOrUnlikePost(postId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/bookmarks")
    public ResponseEntity<Void> toggleBookMark(@PathVariable Long postId,
                                               @AuthenticationPrincipal UserAuth user) {
        postBookmarkService.bookmarkOrUnbookmark(postId, user);
        return ResponseEntity.noContent().build();
    }
}
