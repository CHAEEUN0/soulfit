package soulfit.soulfit.community.post;



import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.post.dto.PostRequestDto;
import soulfit.soulfit.community.post.dto.PostResponseDto;
import soulfit.soulfit.community.like.PostLikeService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;


    //전체 조회
    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPosts(Pageable pageable){
        Page<PostResponseDto> dtoPage = postService.findAllPosts(pageable)
                .map(PostResponseDto::from);
        return ResponseEntity.ok(dtoPage);
    }

    //단건 조회
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId){
        Post post = postService.findPostById(postId);
        return ResponseEntity.ok(PostResponseDto.from(post));
    }

    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getUserPosts(@AuthenticationPrincipal UserAuth userAuth, Pageable pageable){
        Page<PostResponseDto> dtoPage= postService.findPostByUser(userAuth, pageable)
                .map(PostResponseDto::from);
        return ResponseEntity.ok(dtoPage);
    }

    //작성
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@RequestBody PostRequestDto requestDto, @AuthenticationPrincipal UserAuth userAuth){
        Post post = postService.createPost(requestDto, userAuth);
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponseDto.from(post));
    }

    //수정
    @PutMapping("/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(@PathVariable Long postId, @RequestBody PostRequestDto requestDto, @AuthenticationPrincipal UserAuth userAuth){
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
        return ResponseEntity.ok().build();
    }
}
