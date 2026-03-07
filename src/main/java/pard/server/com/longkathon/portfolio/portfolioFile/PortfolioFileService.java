package pard.server.com.longkathon.portfolio.portfolioFile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pard.server.com.longkathon.s3.AwsS3Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioFileService {
    private final PortfolioFileRepository portfolioFileRepository;
    private final AwsS3Service awsS3Service;

    @Transactional //이미지 s3에 업로드하고, DB에 유지
    public void uploadImage(Long portfolioId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("이미지가 없습니다.");
        }

        for(int i = 0; i < images.size(); i++) {
            MultipartFile file = images.get(i);
            if(file.isEmpty()) continue; //해당 리스트의 파일이 없으면 다음 리스트로 스킵

            String fileName = awsS3Service.uploadFile(file); //s3에 파일 업로드 후 파일 이름 리턴 받음

            boolean isThumb = (i == 0); //첫번째 사진이라 썸네일인지 판단
            PortfolioFile portfolioFile = PortfolioFile.builder()
                    .fileName(fileName)
                    .isThumbnail(isThumb)
                    .portfolioId(portfolioId)
                    .build();
            portfolioFileRepository.save(portfolioFile);
        }
    }

    //하나의 포폴의 썸넬 URL을 리턴
    public String getThumbURL(Long portfolioId) {
        // 썸네일이 없는 경우 500 에러 대신 빈 문자열 반환하여 프론트엔드가 디폴트 이미지 표시하도록 함
        return portfolioFileRepository.findByPortfolioIdAndIsThumbnail(portfolioId, true)
                .map(image -> awsS3Service.getFileUrl(image.getFileName()))
                .orElse("");
    }
}
