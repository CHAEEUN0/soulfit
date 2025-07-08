package soulfit.soulfit.meeting.domain;

public enum Category {
    HOBBY("취미"),
    WORKOUT("운동"),
    TRIP("여행"),
    STUDY("스터디"),
    LANGUAGE("외국어"),
    FOOD("음식"),
    ETC("기타");

    private String description;
    Category(String description) {
        this.description = description;
    }
}
