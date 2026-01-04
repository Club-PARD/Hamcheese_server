package pard.server.com.longkathon.MyPage.userFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pard.server.com.longkathon.s3.AwsS3Service;

@Service
@RequiredArgsConstructor
public class UserFileService {
    private final UserFileRepo userFileRepo;
    private final AwsS3Service awsS3Service;

    public void createImageFile (Long userId, String fileName) {
        //회원가입에서 유저가 생성됐을 때 그 사람의 프로필사진을 저장
        // fk로 사용될 userId와 파일명을 받아 저장한다.
        UserFile userFile = UserFile.builder()
                .userId(userId)
                .fileName(fileName) //추후 파일명으로 url을 받는다.
                .build();
        userFileRepo.save(userFile); //저장
    }

    public String getURL(Long userId) { //해당 유저의 프로필사진을 찾아 URL로 변환 후 리턴
        UserFile userFile = userFileRepo.findByUserId(userId);
        if (userFile == null) return null;
        return awsS3Service.getFileUrl(userFile.getFileName());
    }

    @Transactional
    public void updateImageFile(Long userId) {
        UserFile userFile = userFileRepo.findByUserId(userId);
        if (userFile == null) return;

        String oldFileName = userFile.getFileName();   // <- 첫 조회 결과 사용
        userFileRepo.deleteAllByUserId(userId);
        awsS3Service.deleteFile(oldFileName);
    }

}
