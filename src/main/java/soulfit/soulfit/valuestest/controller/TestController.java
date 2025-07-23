package soulfit.soulfit.valuestest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.valuestest.domain.TestType;
import soulfit.soulfit.valuestest.dto.StartTestSessionResponse;
import soulfit.soulfit.valuestest.dto.SubmitAnswerRequest;
import soulfit.soulfit.valuestest.dto.UserTestResult;
import soulfit.soulfit.valuestest.service.TestService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tests")
public class TestController {

    private final TestService testService;

    @PostMapping("/start")
    public ResponseEntity<StartTestSessionResponse> startTest(
            @RequestParam TestType testType,
            @AuthenticationPrincipal UserAuth user) {
        return ResponseEntity.ok(testService.startTest(testType, user));
    }

    @PostMapping("/submit")
    public ResponseEntity<Void> submitAnswers(
            @RequestBody SubmitAnswerRequest dto,
            @AuthenticationPrincipal UserAuth user) {
        testService.submitAnswers(dto, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/results")
    public ResponseEntity<UserTestResult> getUserTestResult(
            @RequestParam TestType testType,
            @AuthenticationPrincipal UserAuth user) {
        return ResponseEntity.ok(testService.getUserTestResult(user, testType));
    }

}
