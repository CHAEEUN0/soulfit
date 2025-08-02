package soulfit.soulfit.authentication.entity;

public enum AccountStatus {
    ACTIVE,      // 정상
    BANNED,      // 제재
    DORMANT,     // 휴면
    PENDING,     // 가입 미완료
    WITHDRAWN,    // 탈퇴
    UNDER_REVIEW // AI에 의해 의심 계정으로 분류되어 관리자 검토가 필요한 상태
}
