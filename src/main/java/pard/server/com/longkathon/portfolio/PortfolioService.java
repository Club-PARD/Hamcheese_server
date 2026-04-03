package pard.server.com.longkathon.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pard.server.com.longkathon.MyPage.user.User;
import pard.server.com.longkathon.MyPage.user.UserRepo;
import pard.server.com.longkathon.likes.likePortfolio.PortfolioLikeRepository;
import pard.server.com.longkathon.portfolio.portfolioFile.PortfolioFileService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final PortfolioFileService portfolioFileService;
    private final UserRepo userRepo;
    private final PortfolioLikeRepository portfolioLikeRepository;

    //-------------------------상세 프로필 페이지 -------------------------------------
    public List<PortfolioDTO.Res1> getPortfolioTabPostOrder (Long userId) {
        // 생성일 기준 최신순으로 정렬된 포트폴리오 목록 조회
        List<Portfolio> portfolioList = portfolioRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        return portfolioList.stream().map(portfolio ->
                PortfolioDTO.Res1.builder()
                        .portfolioId(portfolio.getPortfolioId())
                        .title(portfolio.getTitle())
                        .postDate(portfolio.getCreatedAt())
                        .imageUrl(portfolioFileService.getThumbURL(portfolio.getPortfolioId()))
                        .build())
                .toList();
    }

    public List<PortfolioDTO.Res1> getPortfolioTabRealOrder (Long userId) {
        // 프로젝트 시작일 기준 최신순으로 정렬된 포트폴리오 목록 조회
        List<Portfolio> portfolioList = portfolioRepository.findAllByUserIdOrderByStartDateDesc(userId);

        return portfolioList.stream().map(portfolio ->
                        PortfolioDTO.Res1.builder()
                                .portfolioId(portfolio.getPortfolioId())
                                .title(portfolio.getTitle())
                                .postDate(portfolio.getCreatedAt())
                                .imageUrl(portfolioFileService.getThumbURL(portfolio.getPortfolioId()))
                                .build())
                .toList();
    }

    //-----------------------메인 페이지---------------------------------------------

    /**
     * 좋아요 수 Map 조회 (N+1 방지)
     * @param portfolioIds 포트폴리오 ID 리스트
     * @return portfolioId -> 좋아요 수 매핑
     */
    private Map<Long, Long> getLikeCountMap(List<Long> portfolioIds) {
        if (portfolioIds == null || portfolioIds.isEmpty()) {
            return new HashMap<>();
        }

        List<Object[]> results = portfolioLikeRepository.countLikesByPortfolioIds(portfolioIds);

        Map<Long, Long> likeCountMap = new HashMap<>();
        for (Object[] row : results) {
            Long portfolioId = (Long) row[0];
            Long count = (Long) row[1];
            likeCountMap.put(portfolioId, count);
        }

        // 좋아요가 없는 포트폴리오는 0으로 초기화
        for (Long portfolioId : portfolioIds) {
            likeCountMap.putIfAbsent(portfolioId, 0L);
        }

        return likeCountMap;
    }

    /**
     * Portfolio 엔티티를 DTO로 변환
     * @param portfolio Portfolio 엔티티
     * @return PortfolioDTO.Res1
     */
    private PortfolioDTO.Res1 convertToDTO(Portfolio portfolio) {
        return PortfolioDTO.Res1.builder()
                .portfolioId(portfolio.getPortfolioId())
                .title(portfolio.getTitle())
                .postDate(portfolio.getCreatedAt())
                .imageUrl(portfolioFileService.getThumbURL(portfolio.getPortfolioId()))
                .build();
    }

    /**
     * 포트폴리오 필터링 및 정렬
     * @param departments 학부 리스트 (nullable)
     * @param title 제목 검색어 (nullable)
     * @param firstStudentId 학번 범위 시작 (nullable)
     * @param secondStudentId 학번 범위 끝 (nullable)
     * @param ordering 정렬 방식 ("likeOrder", "realOrder", null)
     * @return 필터링 및 정렬된 포트폴리오 리스트
     */
    @Transactional(readOnly = true)
    public List<PortfolioDTO.Res1> filter(
            List<String> departments,
            String title,
            Long firstStudentId,
            Long secondStudentId,
            String ordering) {

        // 파라미터 존재 여부 확인
        boolean hasDept = departments != null && !departments.isEmpty();
        boolean hasTitle = title != null && !title.isBlank();
        boolean hasStudentRange = firstStudentId != null && secondStudentId != null;

        // 1단계: User 필터링으로 대상 userId 추출
        List<Long> targetUserIds = null;

        if (hasDept || hasStudentRange) {
            List<User> filteredUsers;

            if (hasDept && hasStudentRange) {
                filteredUsers = userRepo.findByDepartmentInAndStudentIdBetween(
                        departments, firstStudentId, secondStudentId);
            } else if (hasDept) {
                filteredUsers = userRepo.findByDepartmentIn(departments);
            } else {
                filteredUsers = userRepo.findByStudentIdBetween(firstStudentId, secondStudentId);
            }

            targetUserIds = filteredUsers.stream()
                    .map(User::getUserId)
                    .toList();

            // User 필터링 결과가 비어있으면 빈 리스트 반환
            if (targetUserIds.isEmpty()) {
                return new ArrayList<>();
            }
        }

        // 2단계: Portfolio Specification 구성
        Specification<Portfolio> spec = Specification.where((Specification<Portfolio>) null);

        if (targetUserIds != null) {
            spec = spec.and(PortfolioSpecification.withUserIds(targetUserIds));
        }

        if (hasTitle) {
            spec = spec.and(PortfolioSpecification.withTitleContaining(title));
        }

        // 3단계: 정렬 및 조회
        List<Portfolio> portfolios;

        if ("likeOrder".equals(ordering)) {
            // 좋아요 순: 정렬 없이 조회 후 메모리 정렬
            portfolios = portfolioRepository.findAll(spec);

            // 좋아요 수 Map 조회 (Batch)
            List<Long> portfolioIds = portfolios.stream()
                    .map(Portfolio::getPortfolioId)
                    .toList();
            Map<Long, Long> likeCountMap = getLikeCountMap(portfolioIds);

            // 좋아요 수 기준 내림차순 정렬
            portfolios.sort((p1, p2) -> {
                Long count1 = likeCountMap.get(p1.getPortfolioId());
                Long count2 = likeCountMap.get(p2.getPortfolioId());
                return count2.compareTo(count1);
            });

        } else {
            // 날짜 순: DB에서 정렬하여 조회
            Sort sort;
            if ("realOrder".equals(ordering)) {
                sort = Sort.by(Sort.Direction.DESC, "startDate");
            } else {
                // ordering이 null이거나 다른 값이면 기본 정렬 (생성일 기준)
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
            }

            portfolios = portfolioRepository.findAll(spec, sort);
        }

        // 4단계: DTO 변환
        return portfolios.stream()
                .map(this::convertToDTO)
                .toList();
    }
}
