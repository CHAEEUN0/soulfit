package soulfit.soulfit.authentication.entity;

public enum AccountStatus {
    ACTIVE,      // 정상
    BANNED,      // 제재
    DORMANT,     // 휴면
    PENDING,     // 가입 미완료
    WITHDRAWN    // 탈퇴
}
