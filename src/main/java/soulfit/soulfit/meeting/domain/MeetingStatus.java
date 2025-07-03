package soulfit.soulfit.meeting.domain;

public enum MeetingStatus {
    OPEN("참여 가능"),
    FULL("모집 마감"),
    COMPLETE("종료"),
    CANCEL("취소");

    private final String description;

    MeetingStatus(String description) {
        this.description = description;
    }



}
