package soulfit.soulfit.chat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ChatImageRequestDto {

    @NotEmpty
    private List<MultipartFile> images;
}
