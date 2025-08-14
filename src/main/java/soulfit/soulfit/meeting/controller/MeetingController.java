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


    /**
     *
     * @param pageable
     * 인기순 /api/meetings?sort=bookmarkCount,desc
     * 최신순 /api/meetings?sort=createAt,desc
     */
    @GetMapping("meetings/recommended")
    public ResponseEntity<List<MeetingResponseDto>> getRecommendedMeetings(@AuthenticationPrincipal UserAuth user) {
        List<MeetingResponseDto> result = meetingService.getRecommendedMeetings(user);
        return ResponseEntity.ok(result);
    }

    @GetMapping("meetings")
    public ResponseEntity<Page<MeetingResponseDto>> getMeetings(Pageable pageable){
        Page<MeetingResponseDto> result = meetingService.getAllMeetings(pageable).map(MeetingResponseDto::from);
        return ResponseEntity.ok(result);
    }



    //최근 참여모임
    @GetMapping("me/meetings/participated")
    public ResponseEntity<Page<MeetingResponseDto>> getParticipatedMeetings(@AuthenticationPrincipal UserAuth user, Pageable pageable){
        Page<MeetingResponseDto> result = meetingService.getParticipatedMeetings(user, pageable).map(MeetingResponseDto::from);

        return ResponseEntity.ok(result);

    }

    //유저가 저장한(북마크) 모임
    @GetMapping("me/meetings/bookmarked")
    public ResponseEntity<Page<MeetingResponseDto>> getUserBookmarkedMeetings(@AuthenticationPrincipal UserAuth user, Pageable pageable){
        Page<MeetingResponseDto> result = meetingBookmarkService.getBookmarkedMeetingsByUser(user, pageable)
                .map(MeetingResponseDto::from);

        return ResponseEntity.ok(result);
    }

    //모임 이름으로 검색
    @GetMapping("meetings/search")
    public ResponseEntity<List<MeetingResponseDto>> searchMeetings(@RequestParam String keyword) {
        List<MeetingResponseDto> result = meetingService.searchMeetingsByTitle(keyword);
        return ResponseEntity.ok(result);
    }

    //조건 검색
    @GetMapping("/meetings/filter")
    public ResponseEntity<Page<MeetingResponseDto>> filterMeetings(SearchFilter filter, Pageable pageable
    ) {
        Page<Meeting> meetings = meetingService.filterMeetings(filter, pageable);
        return ResponseEntity.ok(meetings.map(MeetingResponseDto::from));
    }

    @GetMapping("meetings/{meetingId}")
    public ResponseEntity<MeetingResponseDto> getMeeting(@PathVariable Long meetingId) {
        Meeting meeting = meetingService.getMeetingById(meetingId);
        return ResponseEntity.ok(MeetingResponseDto.from(meeting));
    }


    @PostMapping("meetings")
    public ResponseEntity<MeetingResponseDto> createMeeting(@ModelAttribute @Valid MeetingRequestDto request, @AuthenticationPrincipal UserAuth userAuth) {

        Meeting meeting = meetingService.createMeeting(request, userAuth);

        return ResponseEntity.ok(MeetingResponseDto.from(meeting));
    }


    @PatchMapping("meetings/{meetingId}")
    public ResponseEntity<MeetingResponseDto> updateMeeting(@PathVariable Long meetingId, @ModelAttribute @Valid MeetingUpdateRequestDto request, @AuthenticationPrincipal UserAuth userAuth) {

        Meeting updatedMeeting = meetingService.updateMeeting(meetingId, request, userAuth);
        return ResponseEntity.ok(MeetingResponseDto.from(updatedMeeting));
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
