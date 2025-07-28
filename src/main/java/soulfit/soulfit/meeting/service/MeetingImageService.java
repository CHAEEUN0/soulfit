package soulfit.soulfit.meeting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import soulfit.soulfit.common.S3Uploader;
import soulfit.soulfit.meeting.domain.Meeting;
import soulfit.soulfit.meeting.domain.MeetingImage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingImageService {

    @Autowired
    private final S3Uploader s3Uploader;

    public List<MeetingImage> uploadImages(List<MultipartFile> images, Meeting meeting) {
        List<MeetingImage> meetingImages = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            String key = createKeyName(image.getOriginalFilename());
            String imageUrl = s3Uploader.upload(image, key);

            meetingImages.add(MeetingImage.builder()
                    .meeting(meeting)
                    .imageUrl(imageUrl)
                    .imageKey(key)
                    .order(i)
                    .build());
        }

        return meetingImages;
    }

    public void deleteImages(List<MeetingImage> images){
        for (MeetingImage image : images) {
            s3Uploader.delete(image.getImageKey());
        }
    }

    private String createKeyName(String originalFilename) {
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "meeting/" + UUID.randomUUID() + ext;
    }


}
