package com.belka.audio.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EntityScan(AudioConfig.PACKAGE_NAME)
@EnableJpaRepositories(AudioConfig.PACKAGE_NAME)
@EnableScheduling
public class AudioConfig {
    public static final String PACKAGE_NAME = "com.belka.audio";
}
