package soulfit.soulfit.meeting.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.dto.MeetingRequestDto;
import soulfit.soulfit.meeting.dto.MeetingResponseDto;
import soulfit.soulfit.meeting.dto.MeetingUpdateRequestDto;
import soulfit.soulfit.meeting.dto.SearchFilter;
import soulfit.soulfit.meeting.service.MeetingBookmarkService;
import soulfit.soulfit.meeting.service.MeetingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingBookmarkService meetingBookmarkService;

    @GetMapping("meetings/recommended")
    public ResponseEntity<List<MeetingResponseDto>> getRecommendedMeetings(@AuthenticationPrincipal UserAuth user) {
        List<MeetingResponseDto> result = meetingService.getRecommendedMeetings(user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("meetings")
    public ResponseEntity<Page<MeetingResponseDto>> getMeetings(Pageable pageable){
        Page<MeetingResponseDto> result = meetingService.getAllMeetings(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("meetings/popular")
    public ResponseEntity<List<MeetingResponseDto>> getPopularMeetings(Pageable pageable) {
        List<MeetingResponseDto> result = meetingService.getPopularMeetings(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("meetings/recent")
    public ResponseEntity<List<MeetingResponseDto>> getRecentMeetings(Pageable pageable) {
        List<MeetingResponseDto> result = meetingService.getRecentMeetings(pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("me/meetings/participated")
    public ResponseEntity<Page<MeetingResponseDto>> getParticipatedMeetings(@AuthenticationPrincipal UserAuth user, Pageable pageable){
        Page<MeetingResponseDto> result = meetingService.getParticipatedMeetings(user, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("me/meetings/bookmarked")
    public ResponseEntity<Page<MeetingResponseDto>> getUserBookmarkedMeetings(@AuthenticationPrincipal UserAuth user, Pageable pageable){
        Page<MeetingResponseDto> result = meetingBookmarkService.getBookmarkedMeetingsByUser(user, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("meetings/search")
    public ResponseEntity<List<MeetingResponseDto>> searchMeetings(@RequestParam String keyword) {
        List<MeetingResponseDto> result = meetingService.searchMeetingsByTitle(keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/meetings/filter")
    public ResponseEntity<Page<MeetingResponseDto>> filterMeetings(SearchFilter filter, Pageable pageable) {
        Page<MeetingResponseDto> meetings = meetingService.filterMeetings(filter, pageable);
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("meetings/{meetingId}")
    public ResponseEntity<MeetingResponseDto> getMeeting(@PathVariable Long meetingId) {
        MeetingResponseDto meetingDto = meetingService.getMeetingById(meetingId);
        return ResponseEntity.ok(meetingDto);
    }

    @PostMapping("meetings")
    public ResponseEntity<MeetingResponseDto> createMeeting(@ModelAttribute @Valid MeetingRequestDto request, @AuthenticationPrincipal UserAuth userAuth) {
        Meeting meeting = meetingService.createMeeting(request, userAuth);
        MeetingResponseDto responseDto = meetingService.getMeetingById(meeting.getId());
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("meetings/{meetingId}")
    public ResponseEntity<MeetingResponseDto> updateMeeting(@PathVariable Long meetingId, @ModelAttribute @Valid MeetingUpdateRequestDto request, @AuthenticationPrincipal UserAuth userAuth) {
        Meeting updatedMeeting = meetingService.updateMeeting(meetingId, request, userAuth);
        MeetingResponseDto responseDto = meetingService.getMeetingById(updatedMeeting.getId());
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("meetings/{meetingId}")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable Long meetingId,
            @AuthenticationPrincipal UserAuth userAuth) {
        meetingService.deleteMeeting(meetingId, userAuth);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("meetings/{meetingId}/bookmarks")
    public ResponseEntity<Void> toggleBookMark(@PathVariable Long meetingId,
                                               @AuthenticationPrincipal UserAuth user) {
        meetingBookmarkService.bookmarkOrUnBookmark(meetingId, user);
        return ResponseEntity.noContent().build();
    }
}
