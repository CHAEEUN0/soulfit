package soulfit.soulfit.meeting.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.dto.MeetingFilter;
import soulfit.soulfit.meeting.dto.MeetingRequest;
import soulfit.soulfit.meeting.dto.MeetingResponse;
import soulfit.soulfit.meeting.response.CommonResponse;
import soulfit.soulfit.meeting.service.MeetingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;


    @GetMapping
    public ResponseEntity<CommonResponse<List<MeetingResponse>>> getAllMeetings(){
        List<MeetingResponse> list = meetingService.getAllMeetings();

        return ResponseEntity.ok(new CommonResponse<>(list));
    }

    //모임 이름으로 검색
    @GetMapping("/search")
    public ResponseEntity<CommonResponse<List<MeetingResponse>>> searchMeetings(
            @RequestParam String keyword) {

        List<MeetingResponse> result = meetingService.searchMeetingsByTitle(keyword);
        return ResponseEntity.ok(new CommonResponse<>(result));
    }

    @GetMapping("/filter")
    public ResponseEntity<CommonResponse<List<MeetingResponse>>> filterMeeting(@ModelAttribute MeetingFilter filter){
        List<MeetingResponse> result = meetingService.filterMeetings(filter);
        return ResponseEntity.ok(new CommonResponse<>(result));
    }


    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getMeeting(@PathVariable Long id) {
        Meeting meeting = meetingService.getMeetingById(id);
        return ResponseEntity.ok(MeetingResponse.from(meeting));
    }



    //모임 생성
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @RequestBody @Valid MeetingRequest request,
            @AuthenticationPrincipal UserAuth userAuth) {

        Meeting meeting = meetingService.createMeeting(userAuth, request);

        return ResponseEntity.ok(MeetingResponse.from(meeting));
    }


    @PutMapping("/{id}")
    public ResponseEntity<MeetingResponse> updateMeeting(@PathVariable Long id,
                                              @RequestBody @Valid MeetingRequest request,
                                              @AuthenticationPrincipal UserAuth userAuth) {

        Meeting updatedMeeting = meetingService.updateMeeting(id, request, userAuth);
        return ResponseEntity.ok(MeetingResponse.from(updatedMeeting));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable Long id,
            @AuthenticationPrincipal UserAuth userAuth) {

        meetingService.deleteMeeting(id, userAuth);
        return ResponseEntity.noContent().build();
    }


}
