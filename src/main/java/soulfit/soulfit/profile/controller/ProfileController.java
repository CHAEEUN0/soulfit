
package soulfit.soulfit.profile.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.profile.dto.PhotoResponse;
import soulfit.soulfit.profile.dto.UpdateUserProfileRequest;
import soulfit.soulfit.profile.dto.UserProfileResponse;
import soulfit.soulfit.profile.service.UserProfileService;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;
    private final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    // 내 프로필 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal(expression = "id") Long userId) {
        UserProfileResponse userProfile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    // 특정 사용자 프로필 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable Long userId) {
        UserProfileResponse userProfile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    // 내 앨범 사진 목록 조회
    @GetMapping("/me/photos")
    public ResponseEntity<List<PhotoResponse>> getMyAlbumPhotos(
            @AuthenticationPrincipal(expression = "id") Long userId) {
        List<PhotoResponse> photos = userProfileService.getAlbumPhotos(userId);
        return ResponseEntity.ok(photos);
    }

    // 특정 사용자 앨범 사진 목록 조회
    @GetMapping("/{userId}/photos")
    public ResponseEntity<List<PhotoResponse>> getAlbumPhotos(
            @PathVariable Long userId) {
        List<PhotoResponse> photos = userProfileService.getAlbumPhotos(userId);
        return ResponseEntity.ok(photos);
    }

    @PutMapping
    public ResponseEntity<Void> updateUserProfile(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestPart("request") UpdateUserProfileRequest request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        userProfileService.updateUserProfile(userId, request, profileImage);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/photos")
    public ResponseEntity<Void> addPhotoToAlbum(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestPart("photo") MultipartFile photoFile) {
        userProfileService.addPhotoToAlbum(userId, photoFile);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<Void> deletePhotoFromAlbum(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long photoId) {
        userProfileService.deletePhotoFromAlbum(userId, photoId);
        return ResponseEntity.ok().build();
    }
}
