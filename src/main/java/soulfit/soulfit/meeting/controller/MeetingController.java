package soulfit.soulfit.meeting.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.dto.MeetingRequest;
import soulfit.soulfit.meeting.dto.MeetingResponse;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.meeting.service.MeetingService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingRepository meetingRepository;

    //모임 조회
    @GetMapping
    public ResponseEntity<List<MeetingResponse>> getMeetings(){
        return ResponseEntity.ok(meetingService.getAllMeetings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getMeeting(@PathVariable Long id){
        return ResponseEntity.ok(meetingService.getMeetingById(id));
    }


    //모임 생성
    @PostMapping
    public ResponseEntity<Long> createMeeting(@RequestBody MeetingRequest request){
        Long meetingId = meetingService.createMeeting(request, 1L);
        return ResponseEntity.ok(meetingId);
    }

}
