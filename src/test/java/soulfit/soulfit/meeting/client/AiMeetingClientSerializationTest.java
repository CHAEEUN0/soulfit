package soulfit.soulfit.meeting.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;
import soulfit.soulfit.meeting.dto.ai.AiReviewRequestDto;
import soulfit.soulfit.meeting.dto.ai.AiReviewResponseDto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
class AiMeetingClientSerializationTest {

    @Autowired
    private AiMeetingClient aiMeetingClient;

    @Autowired
    private RestTemplate aiRestTemplate; // Inject the specific RestTemplate used by AiMeetingClient

    private MockRestServiceServer mockServer;

    // No longer need to inject snakeCaseObjectMapper for generating expectedJson
    // @Autowired
    // private ObjectMapper snakeCaseObjectMapper;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(aiRestTemplate).build();
    }

    @Test
    void analyzeMeetingReview_shouldSerializeCamelCaseToSnakeCase() throws Exception {
        // Given: Create a DTO with a camelCase field (for testing purposes)
        // NOTE: For this test to be meaningful for camelCase to snakeCase conversion,
        // you would temporarily need to change 'created_at' to 'createdAt' in AiReviewRequestDto.ReviewData.
        // After the test, revert the change.

        // Assuming AiReviewRequestDto.ReviewData now has 'createdAt' (camelCase)
        // The test will fail if AiReviewRequestDto.ReviewData still has 'created_at' (snake_case)
        // and the user has not made the temporary change.

        AiReviewRequestDto.ReviewData reviewData1 = new AiReviewRequestDto.ReviewData(
                "Test content 1",
                5,
                DateTimeFormatter.ISO_INSTANT.format(LocalDateTime.of(2023, 1, 1, 10, 0, 0).toInstant(ZoneOffset.UTC))
        );
        AiReviewRequestDto.ReviewData reviewData2 = new AiReviewRequestDto.ReviewData(
                "Test content 2",
                4,
                DateTimeFormatter.ISO_INSTANT.format(LocalDateTime.of(2023, 1, 2, 11, 0, 0).toInstant(ZoneOffset.UTC))
        );
        List<AiReviewRequestDto.ReviewData> reviewDataList = Arrays.asList(reviewData1, reviewData2);
        AiReviewRequestDto requestDto = new AiReviewRequestDto(reviewDataList);

        // Manually construct the expected JSON payload with snake_case for 'created_at'
        String expectedJson = "{\"reviews\":["
                            + "{\"content\":\"Test content 1\",\"rating\":5,\"created_at\":\"2023-01-01T10:00:00Z\"},"
                            + "{\"content\":\"Test content 2\",\"rating\":4,\"created_at\":\"2023-01-02T11:00:00Z\"}"
                            + "]}";

        // Mock the AI server's response
        AiReviewResponseDto mockResponse = new AiReviewResponseDto("Mocked summary from AI server.");
        // The response DTO has 'summary' (camelCase), which will be serialized as 'summary' by snakeCaseObjectMapper
        // if it were used, but we'll just use a simple JSON string for the mock response.
        String mockResponseJson = "{\"summary\":\"Mocked summary from AI server.\"}";


        // Expect a POST request to the AI server URL
        mockServer.expect(requestTo("http://localhost:8000/meeting/analyze-reviews"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Verify that the request body matches the expected JSON (with snake_case)
                .andExpect(content().json(expectedJson))
                .andRespond(withSuccess(mockResponseJson, MediaType.APPLICATION_JSON));

        // When: Call the client method
        AiReviewResponseDto actualResponse = aiMeetingClient.analyzeMeetingReview(requestDto);

        // Then: Verify the response and that the mock server received the request
        mockServer.verify();
        assertEquals(mockResponse.getSummary(), actualResponse.getSummary());
    }
}