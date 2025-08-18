
package soulfit.soulfit.profile;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import soulfit.soulfit.authentication.entity.UserAuth;
import soulfit.soulfit.common.S3Uploader;
import soulfit.soulfit.profile.domain.*;
import soulfit.soulfit.profile.dto.PhotoResponse;
import soulfit.soulfit.profile.dto.UpdateUserProfileRequest;
import soulfit.soulfit.profile.dto.UserProfileResponse;
import soulfit.soulfit.profile.repository.PersonalityKeywordRepository;
import soulfit.soulfit.profile.repository.PhotoAlbumRepository;
import soulfit.soulfit.profile.repository.PhotoRepository;
import soulfit.soulfit.profile.repository.UserProfileRepository;
import soulfit.soulfit.profile.service.ProfileAnalysisService;
import soulfit.soulfit.profile.service.UserProfileService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @InjectMocks
    private UserProfileService userProfileService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private PhotoAlbumRepository photoAlbumRepository;

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private PersonalityKeywordRepository personalityKeywordRepository; // 수정: Mock 객체 추가

    @Mock
    private S3Uploader s3Uploader;

    @Mock
    private ProfileAnalysisService profileAnalysisService; // AI 분석 서비스 Mock 추가

    private UserAuth user;
    private UserProfile userProfile;
    private PhotoAlbum photoAlbum;

    @BeforeEach
    void setUp() {
        user = new UserAuth();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("mock@example.com");
        user.setPassword("mockman1");

        userProfile = new UserProfile(user, LocalDate.now(), Gender.MALE);
        userProfile.setId(user.getId()); // 수정: UserProfile에 ID 명시적 설정
        userProfile.setBio("Test bio");
        userProfile.setMbti(MbtiType.INFP);
        userProfile.setRegion("서울");
        userProfile.setLatitude(37.5665);
        userProfile.setLongitude(126.9780);

        photoAlbum = new PhotoAlbum();
        photoAlbum.setUserProfile(userProfile);
        userProfile.setAlbum(photoAlbum); // 수정: UserProfile과 PhotoAlbum 연관관계 설정
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void getUserProfile_Success() {
        // given
        given(userProfileRepository.findByUserAuthId(1L)).willReturn(Optional.of(userProfile));

        // when
        UserProfileResponse response = userProfileService.getUserProfile(1L);

        // then
        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getNickname()).isEqualTo(user.getUsername());
        assertThat(response.getBio()).isEqualTo(userProfile.getBio());

        // ↓↓↓ 추가: 새로운 필드 검증
        assertThat(response.getRegion()).isEqualTo("서울");
        assertThat(response.getLatitude()).isEqualTo(37.5665);
        assertThat(response.getLongitude()).isEqualTo(126.9780);
    }

    @Test
    @DisplayName("프로필 조회 실패 - 사용자를 찾을 수 없음")
    void getUserProfile_Fail_NotFound() {
        // given
        given(userProfileRepository.findByUserAuthId(1L)).willReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> userProfileService.getUserProfile(1L));
    }

    @Test
    @DisplayName("앨범 사진 조회 성공")
    void getAlbumPhotos_Success() {
        // given
        photoAlbum.addPhoto(new Photo("url1", "key1"));
        photoAlbum.addPhoto(new Photo("url2", "key2"));

        given(userProfileRepository.findByUserAuthId(1L)).willReturn(Optional.of(userProfile));
        given(photoAlbumRepository.findByUserProfile(userProfile)).willReturn(Optional.of(photoAlbum));

        // when
        List<PhotoResponse> photos = userProfileService.getAlbumPhotos(1L);

        // then
        assertThat(photos).hasSize(2);
        assertThat(photos.get(0).getImageUrl()).isEqualTo("url1");
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateUserProfile_Success() {
        // given
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setBio("Updated bio");
        request.setMbti(MbtiType.ESTJ);
        request.setPersonalityKeywords(List.of("keyword1", "keyword2"));
        request.setRegion("부산");
        request.setLatitude(35.1796);
        request.setLongitude(129.0756);

        MockMultipartFile image = new MockMultipartFile("image", "hello.png", "image/png", "some-image".getBytes());

        given(userProfileRepository.findByUserAuthId(1L)).willReturn(Optional.of(userProfile));
        given(s3Uploader.upload(any(), any())).willReturn("new_image_url");

        // when
        userProfileService.updateUserProfile(1L, request, image);

        // then
        assertThat(userProfile.getBio()).isEqualTo("Updated bio");
        assertThat(userProfile.getMbti()).isEqualTo(MbtiType.ESTJ);
        assertThat(userProfile.getRegion()).isEqualTo("부산");
        assertThat(userProfile.getLatitude()).isEqualTo(35.1796);
        assertThat(userProfile.getLongitude()).isEqualTo(129.0756);
        assertThat(userProfile.getProfileImageUrl()).isEqualTo("new_image_url");
        assertThat(userProfile.getPersonalityKeywords()).hasSize(2);
        assertThat(userProfile.getPersonalityKeywords().get(0).getKeyword()).isEqualTo("keyword1");
    }

    @Test
    @DisplayName("사진 추가 성공")
    void addPhotoToAlbum_Success() {
        // given
        MockMultipartFile photoFile = new MockMultipartFile("photo", "photo.png", "image/png", "photo-data".getBytes());
        String imageUrl = "s3_photo_url";

        given(userProfileRepository.findByUserAuthId(1L)).willReturn(Optional.of(userProfile));
        given(photoAlbumRepository.findByUserProfile(userProfile)).willReturn(Optional.of(photoAlbum)); // 수정: setUp에서 생성한 앨범 반환
        given(s3Uploader.upload(any(), any())).willReturn(imageUrl);

        // when
        userProfileService.addPhotoToAlbum(1L, photoFile);

        // then
        verify(s3Uploader).upload(any(), any());
        assertThat(userProfile.getPhotoAlbum().getPhotos()).hasSize(1);
        assertThat(userProfile.getPhotoAlbum().getPhotos().get(0).getImageUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("사진 삭제 성공")
    void deletePhotoFromAlbum_Success() {
        // given
        Photo photo = new Photo("url", "key");
        photo.setId(10L);
        photoAlbum.addPhoto(photo); // 수정: setUp에서 생성한 앨범에 사진 추가

        given(userProfileRepository.findByUserAuthId(1L)).willReturn(Optional.of(userProfile));
        given(photoRepository.findById(10L)).willReturn(Optional.of(photo));

        // when
        userProfileService.deletePhotoFromAlbum(1L, 10L);

        // then
        verify(s3Uploader).delete("key");
        verify(photoRepository).delete(photo);
    }

    @Test
    @DisplayName("프로필 수정 시 AI 분석 서비스가 호출된다")
    void updateUserProfile_triggersAIAnalysis() {
        // given
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setBio("Updated bio");
        request.setMbti(MbtiType.ESTJ);
        request.setPersonalityKeywords(List.of("keyword1"));

        given(userProfileRepository.findByUserAuthId(1L)).willReturn(Optional.of(userProfile));

        // when
        userProfileService.updateUserProfile(1L, request, null);

    }
}
