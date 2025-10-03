package soulfit.soulfit.matching.voting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // MultipartFile 임포트 추가
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.common.ImageUploadService; // ImageUploadService 임포트 추가
import soulfit.soulfit.matching.voting.dto.VoteFormCreateRequest;
import soulfit.soulfit.matching.voting.dto.VoteFormResponse;
import soulfit.soulfit.matching.voting.dto.VoteRequest;
import soulfit.soulfit.matching.voting.dto.VoteResultResponse;
import soulfit.soulfit.matching.voting.service.VoteService;

import java.io.IOException; // IOException 임포트 추가

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;
    private final ImageUploadService imageUploadService; // ImageUploadService 주입

    @GetMapping("/forms")
    public ResponseEntity<Page<VoteFormResponse>> getAllVoteForms(@PageableDefault(size = 10) Pageable pageable) {
        Page<VoteFormResponse> voteForms = voteService.getAllVoteForms(pageable);
        return ResponseEntity.ok(voteForms);
    }

    @GetMapping("/{voteFormId}")
    public ResponseEntity<VoteFormResponse> getVoteForm(@PathVariable Long voteFormId) {
        return ResponseEntity.ok(voteService.getVoteForm(voteFormId));
    }

    @PostMapping
    public ResponseEntity<Void> vote(@AuthenticationPrincipal UserAuth user,
                                     @RequestBody VoteRequest request) {
        voteService.vote(user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/forms", consumes = {"multipart/form-data"})
    public ResponseEntity<Long> createVoteForm(@AuthenticationPrincipal UserAuth user,
                                               @RequestPart("request") VoteFormCreateRequest request,
                                               @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = imageUploadService.uploadImage(image, "vote-images/");
        }

        request.setImageUrl(imageUrl);

        Long voteFormId = voteService.createVoteForm(user, request);
        return ResponseEntity.created(java.net.URI.create("/api/votes/forms/" + voteFormId)).body(voteFormId);
    }

    @GetMapping("/forms/{voteFormId}/results")
    public ResponseEntity<VoteResultResponse> getVoteResults(@AuthenticationPrincipal UserAuth user, @PathVariable Long voteFormId) {
        VoteResultResponse results = voteService.getVoteResults(user, voteFormId);
        return ResponseEntity.ok(results);
    }
}
