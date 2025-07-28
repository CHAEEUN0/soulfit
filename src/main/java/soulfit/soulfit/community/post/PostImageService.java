package soulfit.soulfit.community.post;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.common.S3Uploader;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostImageService {

    private final S3Uploader s3Uploader;

    public List<PostImage> uploadImages(List<MultipartFile> images, Post post) {
        List<PostImage> postImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            String key = createKeyName(image.getOriginalFilename());
            String imageUrl = s3Uploader.upload(image, key);

            postImages.add(PostImage.builder()
                    .post(post)
                    .imageUrl(imageUrl)
                    .imageKey(key)
                    .order(i)
                    .build());

        }
        return postImages;
    }

    public void deleteImages(List<PostImage> images) {
        for (PostImage image : images) {
            s3Uploader.delete(image.getImageKey());
        }
    }

    private String createKeyName(String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "post/" + UUID.randomUUID() + ext;
    }
}
