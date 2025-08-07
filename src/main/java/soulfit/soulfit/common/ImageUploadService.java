package soulfit.soulfit.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    private final S3Uploader s3Uploader;

    /**
     * 이미지를 S3에 업로드하고, 업로드된 이미지의 URL을 반환합니다.
     *
     * @param imageFile 업로드할 이미지 파일 (MultipartFile)
     * @param directory S3 버킷 내에서 이미지를 저장할 디렉토리 경로 (예: "profile-images/", "post-images/")
     * @return 업로드된 이미지의 URL
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public String uploadImage(MultipartFile imageFile, String directory) throws IOException {
        if (imageFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지가 없습니다.");
        }

        // 파일명 생성 (중복 방지를 위해 UUID 사용)
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = directory + UUID.randomUUID().toString() + fileExtension;

        return s3Uploader.upload(imageFile, fileName);
    }

    /**
     * S3에서 이미지를 삭제합니다.
     *
     * @param imageUrl 삭제할 이미지의 URL
     */
    public void deleteImage(String imageUrl) {
        // URL에서 S3 key 추출 로직 필요 (예: "https://your-bucket.s3.amazonaws.com/directory/uuid.jpg" -> "directory/uuid.jpg")
        // 현재 S3Uploader의 delete 메서드는 key를 받으므로, URL에서 key를 파싱해야 합니다.
        // 이 부분은 실제 S3 URL 구조에 따라 구현해야 합니다.
        String key = extractKeyFromUrl(imageUrl);
        if (key != null && !key.isEmpty()) {
            s3Uploader.delete(key);
        }
    }

    private String extractKeyFromUrl(String imageUrl) {
        // S3 URL에서 객체 키를 추출합니다.
        // S3Uploader.getUrl() 메서드가 생성하는 URL 형식(예: https://<bucket-name>.s3.<region>.amazonaws.com/<key>)을 가정합니다.
        try {
            java.net.URL url = new java.net.URL(imageUrl);
            String path = url.getPath();
            // 경로의 첫 번째 '/'를 제거하여 key를 얻습니다. (예: "/directory/uuid.jpg" -> "directory/uuid.jpg")
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (java.net.MalformedURLException e) {
            // 유효하지 않은 URL 처리
            return null;
        }
    }
}