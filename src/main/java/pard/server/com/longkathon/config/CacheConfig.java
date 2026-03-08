package pard.server.com.longkathon.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    /**
     * CacheManager 빈 설정
     * ConcurrentMapCacheManager를 사용하여 간단한 인메모리 캐시 구현
     *
     * 사용 중인 캐시 이름:
     * - userProfileImages: 사용자 프로필 이미지 URL 캐싱 (UserFileService에서 사용)
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("userProfileImages");
    }
}
