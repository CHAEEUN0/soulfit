package soulfit.soulfit.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.profile.domain.PhotoAlbum;
import soulfit.soulfit.profile.domain.UserProfile;

import java.util.Optional;

public interface PhotoAlbumRepository extends JpaRepository<PhotoAlbum, Long> {
    Optional<PhotoAlbum> findByUserProfile(UserProfile userProfile);
}