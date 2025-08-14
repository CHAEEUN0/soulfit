package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.meeting.client.AiMeetingClient;
import soulfit.soulfit.meeting.domain.ApprovalStatus;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingParticipant;
import soulfit.soulfit.meeting.dto.ai.AiAnalyzeParticipantRequest;
import soulfit.soulfit.meeting.dto.ai.AiAnalyzeParticipantResponse;
import soulfit.soulfit.meeting.repository.MeetingRepository;
import soulfit.soulfit.profile.domain.UserProfile;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingAiService {

    private final MeetingRepository meetingRepository;
    private final AiMeetingClient aiMeetingClient;

    @Transactional(readOnly = true)
    public AiAnalyzeParticipantResponse analyzeParticipants(Long meetingId) {
        // 1. 모임 정보 조회
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found with id: " + meetingId));

        // 2. 승인된 참가자의 UserProfile 목록 조회
        List<UserProfile> approvedUserProfiles = meeting.getMeetingParticipants().stream()
                .filter(p -> p.getApprovalStatus() == ApprovalStatus.APPROVED)
                .map(MeetingParticipant::getUser)
                .filter(Objects::nonNull)
                .map(UserAuth::getUserProfile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 3. 참가자 데이터 가공 (성별, 연령대)
        Map<String, Integer> genderCounts = new HashMap<>();
        Map<String, Integer> ageBandCounts = new HashMap<>();

        for (UserProfile profile : approvedUserProfiles) {
            // 성별 집계
            if (profile.getGender() != null) {
                String gender = profile.getGender().name();
                genderCounts.put(gender, genderCounts.getOrDefault(gender, 0) + 1);
            }

            // 연령대 집계
            String ageBand = getAgeBand(profile.getBirthDate());
            ageBandCounts.put(ageBand, ageBandCounts.getOrDefault(ageBand, 0) + 1);
        }

        // 4. AI 서버에 보낼 Request DTO 생성
        AiAnalyzeParticipantRequest requestDto = AiAnalyzeParticipantRequest.builder()
                .meetingId(meetingId)
                .genderCounts(genderCounts)
                .ageBandCounts(ageBandCounts)
                .build();

        // 5. AiMeetingClient를 통해 AI 서버에 분석 요청
        return aiMeetingClient.analyzeParticipants(requestDto);
    }

    /**
     * 생년월일을 기반으로 연령대 문자열을 반환하는 헬퍼 메서드
     * @param birthDate 생년월일
     * @return "TEENS", "TWENTIES", "THIRTIES", "FORTIES_ABOVE", "UNKNOWN"
     */
    private String getAgeBand(LocalDate birthDate) {
        if (birthDate == null) {
            return "UNKNOWN";
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 20) {
            return "TEENS";
        } else if (age < 30) {
            return "TWENTIES";
        } else if (age < 40) {
            return "THIRTIES";
        } else {
            return "FORTIES_ABOVE";
        }
    }
}
