package pard.server.com.longkathon.portfolio;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Portfolio 엔티티에 대한 JPA Specification 정의
 * 동적 쿼리 조건을 조합하여 복잡한 필터링을 수행합니다.
 */
public class PortfolioSpecification {

    /**
     * userIds IN 조건
     * Portfolio의 userId가 주어진 userIds 리스트에 포함되는지 확인
     *
     * @param userIds 필터링할 userId 리스트
     * @return Specification<Portfolio>
     */
    public static Specification<Portfolio> withUserIds(List<Long> userIds) {
        return (root, query, cb) -> {
            if (userIds == null || userIds.isEmpty()) {
                return cb.conjunction(); // 조건 없음 (항상 true)
            }
            return root.get("userId").in(userIds);
        };
    }

    /**
     * title LIKE 조건
     * Portfolio의 title이 주어진 문자열을 포함하는지 확인
     *
     * @param title 검색할 제목 문자열
     * @return Specification<Portfolio>
     */
    public static Specification<Portfolio> withTitleContaining(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isBlank()) {
                return cb.conjunction(); // 조건 없음 (항상 true)
            }
            return cb.like(root.get("title"), "%" + title + "%");
        };
    }
}
