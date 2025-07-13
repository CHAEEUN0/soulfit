package soulfit.soulfit.community.post;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.community.post.dto.PostRequestDto;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public Page<Post> findAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Post findPostById(Long postId){
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
    }

    //유저가 쓴글 목록
    @Transactional(readOnly = true)
    public Page<Post> findPostByUser(UserAuth userAuth, Pageable pageable){
        return postRepository.findAllByPoster(userAuth, pageable);
    }



    @Transactional
    public Post createPost(PostRequestDto requestDto, UserAuth userAuth){
        Post post = Post.builder().
                content(requestDto.getContent())
                .poster(userAuth)
                .build();


        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(PostRequestDto requestDto, Long postId, UserAuth userAuth){

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));

        if (!post.getPoster().getId().equals(userAuth.getId())){
            throw new RuntimeException("삭제 권한이 업습니다.");
        }

        post.updateContent(requestDto.getContent());

        return post;
    }

    @Transactional
    public void deletePost(Long postId, UserAuth userAuth){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 존재하지 않습니다."));
        if (!post.getPoster().getId().equals(userAuth.getId())){
            throw new RuntimeException("삭제 권한이 업습니다.");
        }

        postRepository.deleteById(postId);
    }

}
