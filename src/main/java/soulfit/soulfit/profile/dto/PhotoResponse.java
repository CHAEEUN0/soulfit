
package soulfit.soulfit.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import soulfit.soulfit.profile.domain.Photo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {
    private Long id;
    private String imageUrl;

    public static PhotoResponse from(Photo photo) {
        return new PhotoResponse(photo.getId(), photo.getImageUrl());
    }
}
