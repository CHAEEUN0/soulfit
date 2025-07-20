package soulfit.soulfit.community.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class S3UploaderTest {

    @Autowired
    private S3Uploader s3Uploader;

    @Test
    void 사진업로드() throws Exception {
        String filePath = "C:\\Users\\djw72\\바탕 화면\\개1.jpeg";
        File file = new File(filePath);


        FileInputStream input = new FileInputStream(file);
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                file.getName(),
                "image/jpeg",
                input
        );

        String key = "test/" + file.getName();

        // 3. 업로드
        String url = s3Uploader.upload(multipartFile, key);
        System.out.println("URL: " + url);

    }

    @Test
    void 사진삭제(){
        s3Uploader.delete("test/개1.jpeg");
    }
}
