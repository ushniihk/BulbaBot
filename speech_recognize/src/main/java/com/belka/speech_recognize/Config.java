package com.belka.speech_recognize;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EntityScan("com.belka.speech_recognize")
@EnableJpaRepositories("com.belka.speech_recognize")
public class Config {
}
