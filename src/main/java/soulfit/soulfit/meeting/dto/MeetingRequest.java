package soulfit.soulfit.meeting.dto;

import soulfit.soulfit.meeting.domain.Meeting;

public class MeetingRequest {

    public static MeetingRequest from(Meeting meeting) {
        return new MeetingRequest();
    }
}
