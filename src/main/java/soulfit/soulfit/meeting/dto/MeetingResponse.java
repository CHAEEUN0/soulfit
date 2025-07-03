package soulfit.soulfit.meeting.dto;

import soulfit.soulfit.meeting.domain.Meeting;

public class MeetingResponse {

    public static MeetingResponse from(Meeting meeting) {
        return new MeetingResponse();
    }

}
