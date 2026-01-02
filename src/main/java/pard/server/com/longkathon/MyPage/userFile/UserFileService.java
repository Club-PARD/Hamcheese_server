package pard.server.com.longkathon.MyPage.userFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import pard.server.com.longkathon.s3.AwsS3Service;

@Service
@RequiredArgsConstructor
public class UserFileService {
    private final UserFileRepo userFileRepo;
    private final AwsS3Service awsS3Service;

    public String getURL(Long userId) {
        UserFile userFile = userFileRepo.findByUserId(userId);
        if (userFile == null) return null;
        return awsS3Service.getFileUrl(userFile.getFileName());
    }
}
