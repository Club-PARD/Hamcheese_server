package pard.server.com.longkathon.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 시간 관련 유틸리티 클래스
 */
public class TimeUtils {

    /**
     * 특정 시간과 현재 시간의 차이를 상대적인 문자열로 반환
     * 예: "방금전", "3분전", "1시간전", "2일전"
     *
     * @param date 비교할 과거 시간
     * @return 상대 시간 문자열 (null이면 null 반환)
     */
    public static String toRelativeTime(LocalDateTime date) {
        if (date == null) return null;

        LocalDateTime now = LocalDateTime.now(); // 서버 기준 시간
        Duration d = Duration.between(date, now);

        long seconds = d.getSeconds();
        if (seconds < 0) seconds = 0;

        if (seconds < 60) return "방금전";

        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "분전";

        long hours = minutes / 60;
        if (hours < 24) return hours + "시간전";

        long days = hours / 24;
        if (days < 7) return days + "일전";

        long weeks = days / 7;
        if (weeks < 5) return weeks + "주전";

        long months = days / 30;
        if (months < 12) return months + "개월전";

        long years = days / 365;
        return years + "년전";
    }
}
