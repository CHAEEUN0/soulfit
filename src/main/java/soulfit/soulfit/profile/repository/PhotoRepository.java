package soulfit.soulfit.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import soulfit.soulfit.profile.domain.Photo;
import soulfit.soulfit.profile.domain.PhotoAlbum;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    void deleteByIdAndAlbum(Long id, PhotoAlbum album);
}
