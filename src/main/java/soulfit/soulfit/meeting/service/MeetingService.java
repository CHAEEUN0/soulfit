package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingMember;
import soulfit.soulfit.meeting.dto.MeetingRequest;
import soulfit.soulfit.meeting.dto.MeetingResponse;
import soulfit.soulfit.meeting.repository.MeetingRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;

    public List<MeetingResponse> getAllMeetings(){
        return meetingRepository.findAll().stream()
                .map(MeetingResponse::from)
                .collect(Collectors.toList());

    }

    public MeetingResponse getMeetingById(Long id){
        return meetingRepository.findById(id)
                .map(MeetingResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("모임 없음"));
    }

    public Long createMeeting(MeetingRequest request, Long userId){

        //아직
        Meeting meeting = new Meeting();
        meetingRepository.save(meeting);

        return meeting.getId();
    }


}
