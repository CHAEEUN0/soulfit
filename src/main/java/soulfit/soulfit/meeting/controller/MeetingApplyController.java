package soulfit.soulfit.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.dto.*;
import soulfit.soulfit.meeting.response.CommonResponse;
import soulfit.soulfit.meeting.service.MeetingApplyService;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingApplyController {

    private final MeetingApplyService meetingApplyService;

    @PostMapping("/{meetingId}/questions")
    public ResponseEntity<Void> addMeetingQuestions(
            @PathVariable Long meetingId,
            @RequestBody MeetingQuestionDto.Request request) {
        meetingApplyService.addMeetingQuestions(meetingId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{meetingId}/questions")
    public ResponseEntity<List<MeetingQuestionDto.Response>> getMeetingQuestions(@PathVariable Long meetingId) {
        List<MeetingQuestionDto.Response> questions = meetingApplyService.getMeetingQuestions(meetingId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{meetingId}/answers")
    public ResponseEntity<Void> saveAnswers(
            @PathVariable Long meetingId,
            @RequestBody MeetingAnswerDto.Request request,
            @AuthenticationPrincipal UserAuth userAuth) {
        meetingApplyService.saveAnswers(meetingId, userAuth.getId(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{meetingId}/answers")
    public ResponseEntity<List<MeetingAnswerDto.Response>> getAllAnswers(@PathVariable Long meetingId) {
        List<MeetingAnswerDto.Response> answers = meetingApplyService.getAllAnswers(meetingId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/{meetingId}/users/{userId}/answers")
    public ResponseEntity<MeetingAnswerDto.Response> getUserAnswers(
            @PathVariable Long meetingId,
            @PathVariable Long userId) {
        MeetingAnswerDto.Response answer = meetingApplyService.getUserAnswers(meetingId, userId);
        return ResponseEntity.ok(answer);
    }

    @PostMapping("/{meetingId}/join")
    public ResponseEntity<Void> joinMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal UserAuth userAuth) {
        meetingApplyService.joinMeeting(meetingId, userAuth.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{meetingId}/participants/{userId}/approve")
    public ResponseEntity<Void> approveMeetingApplication(
            @PathVariable Long meetingId,
            @PathVariable Long userId) {
        meetingApplyService.approveMeetingApplication(meetingId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{meetingId}/applicants")
    public ResponseEntity<List<MeetingApplicantDto>> getMeetingApplicants(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal UserAuth userAuth) {
        List<MeetingApplicantDto> applicants = meetingApplyService.getApplicantsForMeeting(meetingId, userAuth);
        return ResponseEntity.ok(applicants);
    }

    @PostMapping("/{meetingId}/participants/{userId}/reject")
    public ResponseEntity<Void> rejectMeetingApplication(
            @PathVariable Long meetingId,
            @PathVariable Long userId,
            @RequestBody MeetingRejectRequest request,
            @AuthenticationPrincipal UserAuth userAuth) {
        meetingApplyService.rejectMeetingApplication(meetingId, userId, request.getRejectionReason(), userAuth);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-created")
    public ResponseEntity<CommonResponse<List<MeetingResponse>>> getMyCreatedMeetings(
            @AuthenticationPrincipal UserAuth userAuth) {
        List<MeetingResponse> list = meetingApplyService.getMeetingsByHost(userAuth);
        return ResponseEntity.ok(new CommonResponse<>(list));
    }

}