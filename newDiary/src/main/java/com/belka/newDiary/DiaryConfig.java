package com.belka.newDiary;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.concurrent.TimeUnit;

@Configuration
@EntityScan("com.belka.newDiary.entity")
@EnableJpaRepositories("com.belka.newDiary.repository")
@ComponentScan("com.belka.newDiary")
@EnableCaching
public class DiaryConfig {

    @Value("${diary.cache.expirationTime}")
    private Long expirationTime;

    @Bean(name = "diaryCacheManager")
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("notes");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterAccess(expirationTime, TimeUnit.MINUTES));
        return cacheManager;
    }
}
