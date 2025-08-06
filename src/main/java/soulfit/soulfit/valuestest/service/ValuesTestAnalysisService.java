package soulfit.soulfit.valuestest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import soulfit.soulfit.valuestest.client.AiValuesTestRestTemplateClient;
import soulfit.soulfit.valuestest.domain.TestAnswer;
import soulfit.soulfit.valuestest.domain.TestSession;
import soulfit.soulfit.valuestest.domain.ValuesTestAnalysisReport;
import soulfit.soulfit.valuestest.dto.ai.ValuesTestAnalysisRequestDto;
import soulfit.soulfit.valuestest.dto.ai.ValuesTestAnalysisResponseDto;
import soulfit.soulfit.valuestest.repository.TestAnswerRepository;
import soulfit.soulfit.valuestest.repository.ValuesTestAnalysisReportRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValuesTestAnalysisService {

    private final AiValuesTestRestTemplateClient aiClient;
    private final TestAnswerRepository testAnswerRepository;
    private final ValuesTestAnalysisReportRepository reportRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void analyzeAndSaveReport(TestSession session) {
        // 1. 답변 조회
        List<TestAnswer> answers = testAnswerRepository.findBySessionId(session.getId());

        // 2. AI 요청 DTO 생성
        List<ValuesTestAnalysisRequestDto.AnswerItem> answerItems = answers.stream()
                .map(answer -> ValuesTestAnalysisRequestDto.AnswerItem.builder()
                        .questionId(answer.getQuestion().getId())
                        .questionText(answer.getQuestion().getContent())
                        .selectedChoiceId(answer.getSelectedChoice() != null ? answer.getSelectedChoice().getId() : null)
                        .answerText(answer.getAnswerText())
                        .build())
                .collect(Collectors.toList());

        ValuesTestAnalysisRequestDto requestDto = ValuesTestAnalysisRequestDto.builder()
                .userId(session.getUser().getId())
                .testType(session.getTestType())
                .answers(answerItems)
                .build();

        // 3. AI 서버에 분석 요청
        try {
            ValuesTestAnalysisResponseDto response = aiClient.analyzeValues(requestDto);

            // 4. 결과 리포트 저장
            String topValuesJson = objectMapper.writeValueAsString(response.getTopValues());
            ValuesTestAnalysisReport report = new ValuesTestAnalysisReport(
                    session,
                    response.getAnalysisSummary(),
                    topValuesJson
            );
            reportRepository.save(report);
            log.info("Successfully saved AI analysis report for session: {}", session.getId());

        } catch (JsonProcessingException e) {
            log.error("Error serializing topValues for session {}: {}", session.getId(), e.getMessage());
        } catch (Exception e) {
            log.error("Error while analyzing values test for session {}: {}", session.getId(), e.getMessage());
        }
    }
}
