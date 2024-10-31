package com.belka.weather;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.belka.weather.entity")
@EnableJpaRepositories("com.belka.weather.repository")
public class WeatherConfig {
}
