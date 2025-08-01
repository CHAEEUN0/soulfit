
package soulfit.soulfit.profile.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.common.S3Uploader;
import soulfit.soulfit.profile.domain.PersonalityKeyword;
import soulfit.soulfit.profile.domain.Photo;
import soulfit.soulfit.profile.domain.PhotoAlbum;
import soulfit.soulfit.profile.domain.UserProfile;
import soulfit.soulfit.profile.dto.UpdateUserProfileRequest;
import soulfit.soulfit.profile.repository.PersonalityKeywordRepository;
import soulfit.soulfit.profile.repository.PhotoAlbumRepository;
import soulfit.soulfit.profile.repository.PhotoRepository;
import soulfit.soulfit.profile.repository.UserProfileRepository;

import soulfit.soulfit.profile.dto.PhotoResponse;
import soulfit.soulfit.profile.dto.UserProfileResponse;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final PersonalityKeywordRepository personalityKeywordRepository;
    private final PhotoAlbumRepository photoAlbumRepository;
    private final PhotoRepository photoRepository;
    private final S3Uploader s3Uploader;
    private final Logger logger = LoggerFactory.getLogger(UserProfileService.class);

    private static final String PROFILE_IMAGE_DIR = "profile-images/";
    private static final String ALBUM_DIR = "album-images/";

    // 1. 프로필 정보 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserAuthId(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserProfile not found for user id: " + userId));
        return UserProfileResponse.from(userProfile);
    }

    // 2. 앨범 사진 목록 조회
    @Transactional(readOnly = true)
    public List<PhotoResponse> getAlbumPhotos(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserAuthId(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserProfile not found for user id: " + userId));

        return photoAlbumRepository.findByUserProfile(userProfile)
                .map(photoAlbum -> photoAlbum.getPhotos().stream()
                        .map(PhotoResponse::from)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    // 3. 프로필 정보 수정
    @Transactional
    public void updateUserProfile(Long userId, UpdateUserProfileRequest request, MultipartFile profileImage) {

        UserProfile userProfile = userProfileRepository.findByUserAuthId(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserProfile not found"));

        // 새 프로필 이미지가 있으면 업로드 및 기존 이미지 삭제
        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 이미지 S3에서 삭제
            if (userProfile.getProfileImageUrl() != null && !userProfile.getProfileImageUrl().isEmpty()) {
                try {
                    String oldImageKey = extractKeyFromUrl(userProfile.getProfileImageUrl());
                    s3Uploader.delete(oldImageKey);
                } catch (Exception e) {
                    // 로깅 추가 권장
                    System.err.println("Failed to delete old profile image from S3: " + e.getMessage());
                }
            }
            // 새 이미지 S3에 업로드
            String imageKey = createKey(PROFILE_IMAGE_DIR, profileImage.getOriginalFilename());
            String imageUrl = s3Uploader.upload(profileImage, imageKey);
            userProfile.setProfileImageUrl(imageUrl);
        }

        userProfile.setBio(request.getBio());
        userProfile.setMbti(request.getMbti());

        // 기존 키워드 제거 후 새로운 키워드 추가
        personalityKeywordRepository.deleteAllByUserProfile(userProfile);
        userProfile.getPersonalityKeywords().clear(); // Cascade.ALL 및 orphanRemoval=true를 위해 컬렉션도 클리어
        for (String keyword : request.getPersonalityKeywords()) {
            PersonalityKeyword newKeyword = new PersonalityKeyword(keyword);
            newKeyword.setUserProfile(userProfile);
            userProfile.getPersonalityKeywords().add(newKeyword);
        }
    }

    // 4. 사진 업로드
    @Transactional
    public void addPhotoToAlbum(Long userId, MultipartFile photoFile) {
        UserProfile userProfile = userProfileRepository.findByUserAuthId(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserProfile not found"));

        PhotoAlbum album = photoAlbumRepository.findByUserProfile(userProfile)
                .orElseGet(() -> {
                    PhotoAlbum newAlbum = new PhotoAlbum();
                    newAlbum.setUserProfile(userProfile);
                    return photoAlbumRepository.save(newAlbum);
                });

        String imageKey = createKey(ALBUM_DIR, photoFile.getOriginalFilename());
        String imageUrl = s3Uploader.upload(photoFile, imageKey);

        Photo photo = new Photo(imageUrl, imageKey);
        album.addPhoto(photo); // 양방향 연관관계 설정
    }

    // 5. 사진 삭제
    @Transactional
    public void deletePhotoFromAlbum(Long userId, Long photoId) {
        UserProfile userProfile = userProfileRepository.findByUserAuthId(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserProfile not found"));

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found"));

        // 사용자 확인 (자신의 사진만 삭제 가능)
        if (!photo.getAlbum().getUserProfile().getId().equals(userProfile.getId())) {
            throw new SecurityException("You are not authorized to delete this photo.");
        }

        // S3에서 파일 삭제
        try {
            s3Uploader.delete(photo.getImageKey());
        } catch (Exception e) {
            // 로깅 추가 권장
            System.err.println("Failed to delete photo from S3: " + e.getMessage());
        }

        // 데이터베이스에서 Photo 엔티티 삭제
        photoRepository.delete(photo);
    }

    private String createKey(String dir, String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return dir + UUID.randomUUID() + ext;
    }

    private String extractKeyFromUrl(String imageUrl) {
        // 예: https://<bucket-name>.s3.<region>.amazonaws.com/<key>
        // URL 구조에 따라 파싱 로직을 조정해야 함
        try {
            int lastSlashIndex = imageUrl.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < imageUrl.length() - 1) {
                 String keyWithDir = imageUrl.substring(lastSlashIndex + 1);
                 // URL 인코딩된 문자가 있을 수 있으므로 디코딩이 필요할 수 있음
                 // 여기서는 간단히 처리하지만, 실제로는 URL 디코딩을 고려해야 함
                 // 또한, 디렉토리 구조가 키에 포함되어야 함
                 if (imageUrl.contains(PROFILE_IMAGE_DIR)) {
                     return PROFILE_IMAGE_DIR + keyWithDir;
                 } else if (imageUrl.contains(ALBUM_DIR)) {
                     return ALBUM_DIR + keyWithDir;
                 }
                 // Fallback or error
                 return keyWithDir; // Or throw an exception if structure is unknown
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid S3 URL format", e);
        }
        throw new IllegalArgumentException("Could not extract key from URL: " + imageUrl);
    }
}


