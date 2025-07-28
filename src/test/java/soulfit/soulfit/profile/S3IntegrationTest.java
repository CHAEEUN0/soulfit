package soulfit.soulfit.profile;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.authentication.repository.UserRepository;
import soulfit.soulfit.profile.domain.Gender;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.profile.dto.UpdateUserProfileRequest;
import soulfit.soulfit.profile.repository.UserProfileRepository;
import soulfit.soulfit.profile.service.UserProfileService;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Testcontainers
@SpringBootTest
@Transactional
class S3IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(S3IntegrationTest.class);

    @Container
    static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.2.0"))
                    .withServices(LocalStackContainer.Service.S3);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.s3.endpoint", () -> localStack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("aws.region", localStack::getRegion);
        registry.add("aws.credentials.access-key", localStack::getAccessKey);
        registry.add("aws.credentials.secret-key", localStack::getSecretKey);
    }

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private UserAuth testUser;

    @BeforeAll
    static void beforeAll(@Autowired S3Client s3Client, @Value("${aws.s3.bucket}") String bucketName) {
        log.info("Attempting to create S3 bucket: {}", bucketName);
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("Bucket '{}' created successfully.", bucketName);
        } catch (BucketAlreadyOwnedByYouException e) {
            log.warn("Bucket '{}' already exists. Skipping creation.", bucketName);
        }
    }

    @BeforeEach
    void setUp() {
        log.info("===== Setting up data for new test =====");
        testUser = new UserAuth();
        testUser.setEmail("s3test@example.com");
        testUser.setUsername("s3tester");
        testUser.setPassword("password");
        userRepository.save(testUser);
        log.info("Saved testUser with ID: {}", testUser.getId());

        UserProfile userProfile = new UserProfile(testUser, LocalDate.now(), Gender.FEMALE);
        userProfileRepository.save(userProfile);
        log.info("Saved initial UserProfile for userId: {}", userProfile.getId());
    }

    @Test
    @DisplayName("프로필 수정 시 S3(LocalStack)에 이미지가 성공적으로 업로드된다")
    void updateUserProfile_shouldUploadImageToS3() {
        log.info("--- Test: updateUserProfile_shouldUploadImageToS3 --- START ---");
        // Given
        log.info("[Given] Creating request DTO and mock file");
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setBio("New bio for S3 test");
        request.setMbti(null);
        request.setPersonalityKeywords(Collections.emptyList());

        MockMultipartFile imageFile = new MockMultipartFile(
                "profileImage",
                "test-image.png",
                "image/png",
                "This is a test image".getBytes()
        );
        log.info("Updating profile for userId: {}. Uploading file: {}", testUser.getId(), imageFile.getOriginalFilename());

        // When
        log.info("[When] Calling userProfileService.updateUserProfile");
        userProfileService.updateUserProfile(testUser.getId(), request, imageFile);
        log.info("Service method executed.");

        // Then
        log.info("[Then] Verifying results...");
        UserProfile updatedProfile = userProfileRepository.findByUserAuthId(testUser.getId()).orElseThrow();
        String imageUrl = updatedProfile.getProfileImageUrl();
        log.info("DB check: profileImageUrl is '{}'", imageUrl);

        assertThat(imageUrl).isNotNull();
        assertThat(imageUrl).contains("profile-images/");

        String key = imageUrl.substring(imageUrl.indexOf("profile-images/"));
        log.info("S3 check: Verifying key '{}' in bucket '{}'", key, bucketName);

        assertDoesNotThrow(() -> {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        }, "File should exist in S3 bucket");
        log.info("S3 check: Verification successful. File exists.");
        log.info("--- Test: updateUserProfile_shouldUploadImageToS3 --- END ---");
    }
}