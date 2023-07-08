package com.belka.stats;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.belka.stats")
@EnableJpaRepositories("com.belka.stats")
public class StatsConfig {
}
