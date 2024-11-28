package com.belka.weather.configs;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.belka.weather.entities")
@EnableJpaRepositories("com.belka.weather.repositories")
public class WeatherConfig {
}
