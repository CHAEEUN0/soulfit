package soulfit.soulfit.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class S3Uploader {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String BUCKET_NAME;

    public String upload(MultipartFile multipartFile, String key) {

        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .contentType(multipartFile.getContentType())
                    .contentLength(multipartFile.getSize())
                    .build();
            s3Client.putObject(objectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

            return getUrl(key);


        } catch (IOException e) {
            throw new RuntimeException("s3 파일 업로드 실패");
        }
    }

    private String getUrl(String key) {
        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(key)
                        .build())
                .toExternalForm();
    }


    public void delete(String key){
        try{
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (AwsServiceException | SdkClientException e){
            throw new RuntimeException("s3 파일 삭제 실패", e);
        }

    }

    public void deleteAll(String prefix) {

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(
                ListObjectsV2Request.builder()
                        .bucket(BUCKET_NAME)
                        .prefix(prefix)
                        .build()
        );

        List<ObjectIdentifier> keysToDelete = listResponse.contents().stream()
                .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
                .toList();

        if (keysToDelete.isEmpty()) return;

        Delete delete = Delete.builder()
                .objects(keysToDelete)
                .build();

        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(BUCKET_NAME)
                .delete(delete)
                .build();

        s3Client.deleteObjects(request);
    }


}
