package soulfit.soulfit.matching.voting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.matching.voting.dto.VoteFormCreateRequest;
import soulfit.soulfit.matching.voting.dto.VoteFormResponse;
import soulfit.soulfit.matching.voting.dto.VoteRequest;
import soulfit.soulfit.matching.voting.dto.VoteResultResponse;
import soulfit.soulfit.matching.voting.service.VoteService;

@RestController
@RequestMapping("/api/votes")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

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

    @PostMapping("/forms")
    public ResponseEntity<Long> createVoteForm(@AuthenticationPrincipal UserAuth user, @RequestBody VoteFormCreateRequest request) {
        Long voteFormId = voteService.createVoteForm(user, request);
        return ResponseEntity.created(java.net.URI.create("/api/votes/forms/" + voteFormId)).body(voteFormId);
    }

    @GetMapping("/forms/{voteFormId}/results")
    public ResponseEntity<VoteResultResponse> getVoteResults(@AuthenticationPrincipal UserAuth user, @PathVariable Long voteFormId) {
        VoteResultResponse results = voteService.getVoteResults(user, voteFormId);
        return ResponseEntity.ok(results);
    }
}
