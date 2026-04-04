package pard.server.com.longkathon.portfolio.portfolioFile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pard.server.com.longkathon.s3.AwsS3Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            String originalFileName = file.getOriginalFilename(); // 원본 파일명 저장

            boolean isThumb = (i == 0); //첫번째 사진이라 썸네일인지 판단
            PortfolioFile portfolioFile = PortfolioFile.builder()
                    .fileName(fileName)
                    .originalFileName(originalFileName)
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

    @Transactional
    public void updateImage(Long portfolioId, List<MultipartFile> images) {
        // 1. 입력 검증
        if (images == null) {
            images = new ArrayList<>();
        }

        // 2. 기존 파일 조회
        List<PortfolioFile> existingFiles = portfolioFileRepository.findAllByPortfolioId(portfolioId);

        // 3. 업로드된 파일들의 원본 파일명 수집
        Set<String> uploadedOriginalNames = images.stream()
                .filter(file -> !file.isEmpty())
                .map(MultipartFile::getOriginalFilename)
                .collect(Collectors.toSet());

        // 4. 모든 이미지 삭제 케이스 (업로드된 파일이 없는 경우)
        if (uploadedOriginalNames.isEmpty()) {
            deleteAllImages(portfolioId, existingFiles);
            return;
        }

        // 5. 삭제 대상 파일 처리 (원본 파일명이 새 리스트에 없는 파일)
        for (PortfolioFile existingFile : existingFiles) {
            if (!uploadedOriginalNames.contains(existingFile.getOriginalFileName())) {
                awsS3Service.deleteFile(existingFile.getFileName());
                portfolioFileRepository.delete(existingFile);
            }
        }

        // 6. 기존 파일명 수집 (중복 업로드 방지용)
        Set<String> existingOriginalNames = existingFiles.stream()
                .map(PortfolioFile::getOriginalFileName)
                .collect(Collectors.toSet());

        // 7. 새 파일만 업로드 (기존에 없던 파일)
        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;

            String originalFileName = image.getOriginalFilename();

            // 기존에 없던 파일만 업로드
            if (!existingOriginalNames.contains(originalFileName)) {
                String fileName = awsS3Service.uploadFile(image);

                PortfolioFile portfolioFile = PortfolioFile.builder()
                        .fileName(fileName)
                        .originalFileName(originalFileName)
                        .isThumbnail(false)  // 임시로 false, 나중에 재설정
                        .portfolioId(portfolioId)
                        .build();
                portfolioFileRepository.save(portfolioFile);
            }
        }

        // 8. 썸네일 재설정 (업로드된 파일 순서 기준 첫 번째가 썸네일)
        resetThumbnail(portfolioId, images);
    }

    // 헬퍼 메서드: 모든 이미지 삭제
    private void deleteAllImages(Long portfolioId, List<PortfolioFile> files) {
        for (PortfolioFile file : files) {
            awsS3Service.deleteFile(file.getFileName());
            portfolioFileRepository.delete(file);
        }
    }

    // 헬퍼 메서드: 썸네일 재설정
    private void resetThumbnail(Long portfolioId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;

        // 1. 모든 파일의 썸네일 상태를 false로 초기화
        List<PortfolioFile> allFiles = portfolioFileRepository.findAllByPortfolioId(portfolioId);
        for (PortfolioFile file : allFiles) {
            file.setThumbnail(false);
        }

        // 2. 업로드된 첫 번째 파일의 원본 파일명 찾기
        String firstOriginalFileName = null;
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                firstOriginalFileName = image.getOriginalFilename();
                break;
            }
        }

        // 3. 첫 번째 파일을 썸네일로 설정
        if (firstOriginalFileName != null) {
            String finalFirstOriginalFileName = firstOriginalFileName;
            allFiles.stream()
                    .filter(file -> file.getOriginalFileName().equals(finalFirstOriginalFileName))
                    .findFirst()
                    .ifPresent(file -> file.setThumbnail(true));
        }
    }

    // 해당 포트폴리오에 속한 모든 이미지의 S3 URL 리턴
    public List<String> readImageUrls(Long portfolioId) {
        List<PortfolioFile> imageList = portfolioFileRepository.findAllByPortfolioId(portfolioId);

        return imageList.stream()
                .map(image -> awsS3Service.getFileUrl(image.getFileName()))
                .collect(Collectors.toList());
    }
}
