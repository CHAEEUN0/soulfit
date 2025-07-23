package soulfit.soulfit.community.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class S3UploaderTest {

    @Autowired
    private S3Uploader s3Uploader;

    @Test
    void 사진업로드() throws Exception {
        InputStream fis = getClass().getResourceAsStream("/test1.jpeg");

        MockMultipartFile multipartFile = new MockMultipartFile(
                "images", "test1.jpeg", "image/jpeg", fis);
        String key = "test/test.jpeg";

        // 3. 업로드
        String url = s3Uploader.upload(multipartFile, key);
        System.out.println("URL: " + url);

    }

    @Test
    void 사진삭제(){
        s3Uploader.delete("test/test.jpeg");
    }
}
